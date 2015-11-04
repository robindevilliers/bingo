package uk.co.malbec.hound.reporter;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import uk.co.malbec.hound.Reporter;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.Sampler;
import uk.co.malbec.hound.reporter.machinery.*;

import javax.json.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.util.Collections.emptyMap;
import static java.util.Collections.sort;
import static java.util.function.Function.identity;
import static java.util.stream.IntStream.range;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.Utils.interpolateList;
import static uk.co.malbec.hound.Utils.map;
import static uk.co.malbec.hound.reporter.machinery.Machinery.*;
import static uk.co.malbec.hound.reporter.machinery.Machinery.limit;

public class HtmlReporter implements Reporter {

    private File reportsDirectory = new File(format("%s/reports/%s", System.getProperty("user.dir"), System.currentTimeMillis()));
    private String description = "";
    private DateTime executeTime = now();
    private List<String> bulletPoints = new ArrayList<>();

    private JsonBuilderFactory builderFactory = Json.createBuilderFactory(emptyMap());
    private JsonWriterFactory writerFactory = Json.createWriterFactory(map(PRETTY_PRINTING, true));
    private DecimalFormat decimalFormat = new DecimalFormat("#####0.0000");

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public HtmlReporter setReportsDirectory(File reportsDirectory) {
        if (!reportsDirectory.isDirectory()) {
            reportsDirectory.mkdirs();
        }
        this.reportsDirectory = reportsDirectory;
        return this;
    }

    public HtmlReporter setDescription(String description) {
        this.description = description;
        return this;
    }

    public HtmlReporter setExecuteTime(DateTime executeTime) {
        this.executeTime = executeTime;
        return this;
    }

    public HtmlReporter addBulletPoint(String message) {
        bulletPoints.add(message);
        return this;
    }



    @Override
    public void generate(Sampler sampler) {

        Data data = new Data();

        try {
            sampler.stream().forEach(new FirstPassProcessor(data)::accept);
        } catch (IOException e) {
            throw new RuntimeException("error reading samples", e);
        }

        try {
            sampler.stream().forEach(new SecondPassProcessor(data)::accept);
        } catch (IOException e) {
            throw new RuntimeException("error reading samples", e);
        }

        try {

            long duration = (data.getLatestStartTime().getValue() - data.getEarliestStartTime().getValue()) / 1000;

            JsonArrayBuilder bullets = builderFactory.createArrayBuilder();
            bulletPoints.forEach(bullets::add);
            writeVariableAsFile(reportsDirectory,
                    "profile",
                    builderFactory.createObjectBuilder()
                            .add("description", description)
                            .add("executionTime", executeTime.getMillis())
                            .add("durationInSeconds", duration)
                            .add("bulletPoints", bullets)
                            .build()
            );

            generateIndicatorData(reportsDirectory, data.getIndicatorCategories());
            generateRequestTypesData(reportsDirectory, data.getOperationNameCategories());
            generateStatisticsData(reportsDirectory, data.getOperationNameCategories(), data.getStatistics(), duration);
            generateErrorData(reportsDirectory, data.getStatistics(), data.getErrorMessageCategories());
            generateResponseTimeDistributionsData(reportsDirectory, data.getOperationNameCategories(), data.getStatistics());
            generateUserActivityData(reportsDirectory, data.getTimeDistributionCategories());

            File fontsDirectory = new File(reportsDirectory, "fonts");
            fontsDirectory.mkdir();
            copyFileFromTemplate("index.html", reportsDirectory);
            copyFileFromTemplate("logo.gif", reportsDirectory);
            copyFileFromTemplate("charts.js", reportsDirectory);
            copyFileFromTemplate("underscore.js", reportsDirectory);
            copyFileFromTemplate("jquery.min.js", reportsDirectory);
            copyFileFromTemplate("highcharts.src.js", reportsDirectory);
            copyFileFromTemplate("bootstrap.min.js", reportsDirectory);
            copyFileFromTemplate("bootstrap.min.css", reportsDirectory);
            copyFileFromTemplate("bootstrap.css.map", reportsDirectory);
            copyFileFromTemplate("fonts/glyphicons-halflings-regular.eot", reportsDirectory);
            copyFileFromTemplate("fonts/glyphicons-halflings-regular.svg", reportsDirectory);
            copyFileFromTemplate("fonts/glyphicons-halflings-regular.ttf", reportsDirectory);
            copyFileFromTemplate("fonts/glyphicons-halflings-regular.woff", reportsDirectory);
            copyFileFromTemplate("fonts/glyphicons-halflings-regular.woff2", reportsDirectory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Long time(Sample sample) {
        return sample.getEnd() - sample.getStart();
    }

    public static Long startSecond(Sample sample) {
        return sample.getStart() / 1000;
    }

    private void generateErrorData(File reportsDirectory, Statistics statistics,
                                   CategoryGroup<String, ErrorSummary> errorMessageCategories
    ) throws Exception {

        JsonArrayBuilder errors = builderFactory.createArrayBuilder();
        final AtomicLong index = new AtomicLong(0);
        errorMessageCategories.getKeys().forEach(errorMessage -> {

            JsonArrayBuilder error = builderFactory.createArrayBuilder();
            error.add(errorMessage);
            error.add(errorMessageCategories.get(errorMessage).getTotalCount().getValue());
            error.add(decimalFormat.format((double) errorMessageCategories.get(errorMessage).getTotalCount().getValue() / statistics.getBad().getValue()));
            error.add(index.get());

            errors.add(error);

            StringBuilder errorRows = new StringBuilder();

            errorMessageCategories.get(errorMessage).getErrors().getValues().forEach(sample -> {
                if (errorMessage.equals(sample.getErrorMessage())) {
                    errorRows.append(format("<tr><td>%s</td><td>%s</td></tr>", DateTimeFormat.mediumDateTime().print(sample.getStart()), sample.getDetailedErrorMessage().replace("\n", "<br/>")));
                }
            });

            BufferedReader buffer = new BufferedReader(new InputStreamReader(HtmlReporter.class.getResourceAsStream("/template/error.html")));
            String errorPage = buffer.lines().collect(Collectors.joining("\n")).replace("ERROR_MESSAGE", errorMessage).replace("ERROR_ROWS", errorRows.toString());

            PrintWriter printWriter = null;
            try {
                printWriter = new PrintWriter(new File(reportsDirectory, "error_" + index.get() + ".html"));
                printWriter.print(errorPage);
                printWriter.flush();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }


            index.incrementAndGet();
        });
        writeVariableAsFile(reportsDirectory, "errors", errors.build());
    }

    private void generateUserActivityData(File reportsDirectory, CategoryGroup<Long, CategoryGroup<String, Scalar<Long>>> timeDistributionCategories) throws Exception {
        JsonArrayBuilder series = builderFactory.createArrayBuilder();
        timeDistributionCategories.getKeys()
                .stream()
                .sorted()
                .forEach(key -> series.add(builderFactory.createArrayBuilder()
                                .add(key * 1000)
                                .add(timeDistributionCategories.get(key).getKeys().size()))
                );

        writeVariableAsFile(reportsDirectory, "userActivity",
                series.build()
        );
    }

    private void generateResponseTimeDistributionsData(
            File reportsDirectory,
            CategoryGroup<String, Statistics> operationNameCategories,
            Statistics statistics
    ) throws Exception {

        JsonArrayBuilder categories = builderFactory.createArrayBuilder();
        range(0, (int) (statistics.getMaximumTime().getValue() - statistics.getMinimumTime().getValue()) / 10).forEach(i -> categories.add("" + (i * 10) + "ms"));

        JsonArrayBuilder distributions = builderFactory.createArrayBuilder();
        operationNameCategories.getKeys().forEach(name -> {
            List<Long> timeDistribution = operationNameCategories.get(name).getTimeDistribution().getValues();

            JsonArrayBuilder values = builderFactory.createArrayBuilder();
            range(0, (int) (statistics.getMaximumTime().getValue() - statistics.getMinimumTime().getValue()) / 10).forEach(i -> {
                long count = timeDistribution.stream().filter(j -> (i * 10 + statistics.getMinimumTime().getValue()) <= j && ((i * 10) + 10 + statistics.getMinimumTime().getValue()) > j).count();
                values.add(count);
            });

            distributions.add(
                    builderFactory.createObjectBuilder()
                            .add("name", name)
                            .add("data", values)
            );
        });

        writeVariableAsFile(reportsDirectory, "responseTimeDistributions",
                builderFactory.createObjectBuilder()
                        .add("categories", categories)
                        .add("distributions", distributions)
                        .build()
        );
    }

    private void generateStatisticsData(File reportsDirectory,
                                        CategoryGroup<String, Statistics> operationNameCategories,
                                        Statistics statistics,
                                        long duration
    ) throws Exception {
        JsonArrayBuilder rows = builderFactory.createArrayBuilder();
        rows.add(generateStatisticsForRow("All", statistics, duration));

        operationNameCategories.getKeys().forEach(name ->
                        rows.add(generateStatisticsForRow(name, operationNameCategories.get(name), duration))
        );

        writeVariableAsFile(reportsDirectory, "statistics", rows.build());
    }

    private JsonArrayBuilder generateStatisticsForRow(String title, Statistics statistics, long duration) {
        double percentile95 = interpolateList(95, statistics.getTimeDistribution().getValues());
        double percentile99 = interpolateList(99, statistics.getTimeDistribution().getValues());

        double requestsPerSecond = statistics.getTotalCount().getValue() / duration;

        return builderFactory.createArrayBuilder()
                .add(title)
                .add(statistics.getTotalCount().getValue())
                .add(statistics.getGood().getValue())
                .add(statistics.getBad().getValue())
                .add(decimalFormat.format((float) statistics.getBad().getValue() / statistics.getTotalCount().getValue() * 100))
                .add(statistics.getMin().getValue())
                .add(statistics.getMax().getValue())
                .add(statistics.getMean())
                .add((long) statistics.getStandardError())
                .add((long) statistics.getStandardDeviation())
                .add(decimalFormat.format(percentile95))
                .add(decimalFormat.format(percentile99))
                .add(decimalFormat.format(requestsPerSecond));
    }

    private void generateIndicatorData(File reportsDirectory, CategoryGroup<Long, Scalar<Long>> indicatorCategories) throws Exception {

        writeVariableAsFile(reportsDirectory,
                "indicatorData",
                builderFactory.createArrayBuilder()
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("t < 800ms")
                                        .add(indicatorCategories.get(800L).getValue())
                        )
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("800ms < t < 1200ms")
                                        .add(indicatorCategories.get(1200L).getValue())
                        )
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("1200ms < t")
                                        .add(indicatorCategories.get(Long.MAX_VALUE).getValue())
                        )
                        .build()
        );
    }

    private void generateRequestTypesData(File reportsDirectory, CategoryGroup<String, Statistics> operationNameCategories) throws Exception {
        JsonArrayBuilder data = builderFactory.createArrayBuilder();
        operationNameCategories.getKeys().forEach(name ->
                data.add(builderFactory.createObjectBuilder()
                                .add("name", name)
                                .add("y", operationNameCategories.get(name).getTotalCount().getValue())
                ));

        writeVariableAsFile(
                reportsDirectory,
                "requestTypesData",
                data.build()
        );
    }

    private void copyFileFromTemplate(String fileName, File baseDirectory) throws Exception {
        try {
            InputStream is = HtmlReporter.class.getResourceAsStream("/template/" + fileName);
            copy(is, Paths.get(format("%s/%s", baseDirectory.getAbsolutePath(), fileName)));
        } catch (IOException e) {
            throw new RuntimeException("io error copying file " + fileName, e);
        }
    }

    private void writeVariableAsFile(File reportsDirectory, String variableName, JsonStructure data) throws Exception {
        PrintWriter printWriter = new PrintWriter(new File(reportsDirectory, variableName + ".js"));
        printWriter.print("var " + variableName + " = ");

        JsonWriter jsonWriter = writerFactory.createWriter(printWriter);
        jsonWriter.write(data);
        jsonWriter.close();
    }
}
