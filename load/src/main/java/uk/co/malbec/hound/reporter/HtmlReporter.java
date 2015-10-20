package uk.co.malbec.hound.reporter;

import org.joda.time.DateTime;
import uk.co.malbec.hound.Reporter;
import uk.co.malbec.hound.Sample;

import javax.json.*;
import java.io.*;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.*;

import static java.lang.String.format;
import static java.nio.file.Files.copy;
import static java.util.Collections.emptyMap;
import static java.util.Collections.sort;
import static java.util.stream.Collectors.*;
import static java.util.stream.IntStream.range;
import static javax.json.stream.JsonGenerator.PRETTY_PRINTING;
import static org.joda.time.DateTime.now;
import static uk.co.malbec.hound.Utils.interpolateList;
import static uk.co.malbec.hound.Utils.map;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class HtmlReporter implements Reporter {

    private File reportsDirectory = new File(format("%s/reports/%s", System.getProperty("user.dir"), System.currentTimeMillis()));
    private String description = "";
    private DateTime executeTime = now();
    private List<String> bulletPoints = new ArrayList<>();

    private JsonBuilderFactory builderFactory = Json.createBuilderFactory(emptyMap());
    private JsonWriterFactory writerFactory = Json.createWriterFactory(map(PRETTY_PRINTING, true));
    private DecimalFormat decimalFormat = new DecimalFormat("#####0.0000");


    public HtmlReporter setReportsDirectory(File reportsDirectory){
        if (!reportsDirectory.isDirectory()) {
            reportsDirectory.mkdirs();
        }
        this.reportsDirectory = reportsDirectory;
        return this;
    }

    public HtmlReporter setDescription(String description){
        this.description = description;
        return this;
    }

    public HtmlReporter setExecuteTime(DateTime executeTime){
        this.executeTime = executeTime;
        return this;
    }

    public HtmlReporter addBulletPoint(String message) {
        bulletPoints.add(message);
        return this;
    }

    @Override
    public void generate(List<Sample> allSamples) {
        try {

            JsonArrayBuilder bullets = builderFactory.createArrayBuilder();
            bulletPoints.forEach(bullets::add);

            long durationInSeconds = obtainDurationOfTestInSeconds(allSamples);

            writeVariableAsFile(reportsDirectory,
                    "profile",
                    builderFactory.createObjectBuilder()
                            .add("description", description)
                            .add("executionTime", executeTime.getMillis())
                            .add("durationInSeconds", durationInSeconds)
                            .add("bulletPoints", bullets)
                            .build()
            );



            List<String> names = allSamples.stream().map(Sample::getOperationName).distinct().collect(toList());
            generateIndicatorData(reportsDirectory, allSamples);
            generateRequestTypesData(reportsDirectory, allSamples, names);
            generateStatisticsData(reportsDirectory, allSamples, names, durationInSeconds);
            generateErrorData(reportsDirectory, allSamples);
            generateResponseTimeDistributionsData(reportsDirectory, allSamples, names);
            generateUserActivityData(reportsDirectory, allSamples);

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

    private long obtainDurationOfTestInSeconds(List<Sample> allSamples){
        long earliestTime = Long.MAX_VALUE;
        long latestTime = 0;
        for (Sample sample : allSamples){
            if (sample.getStart() < earliestTime){
                earliestTime = sample.getStart();
            }
            if (sample.getStart() > latestTime){
                latestTime = sample.getStart();
            }
        }

        return (latestTime - earliestTime) / 1000;
    }

    private void generateErrorData(File reportsDirectory, List<Sample> allSamples) throws Exception {

        Map<String, Long> errorData = allSamples.stream().filter(s -> !s.isOk()).collect(groupingBy(Sample::getErrorMessage, counting()));

        Long total = errorData.values().stream().collect(reducing(Long::sum)).orElse(0L);


        JsonArrayBuilder errors = builderFactory.createArrayBuilder();
        errorData.forEach((key, value) -> {
            JsonArrayBuilder error = builderFactory.createArrayBuilder();
            error.add(key);
            error.add(value);
            error.add(decimalFormat.format((double) value / total));
            errors.add(error);
        });
        writeVariableAsFile(reportsDirectory, "errors", errors.build());
    }


    private void generateUserActivityData(File reportsDirectory, List<Sample> allSamples) throws Exception {
        Map<Long, Integer> userCountGroupedByStartTime = allSamples.stream().collect(
                groupingBy(
                        s -> s.getStart() / 1000,
                        collectingAndThen(
                                groupingBy(Sample::getUsername, counting()),
                                Map::size
                        )
                )
        );

        JsonArrayBuilder series = builderFactory.createArrayBuilder();

        userCountGroupedByStartTime.keySet()
                .stream()
                .sorted()
                .forEach(key -> series.add(builderFactory.createArrayBuilder()
                                .add(key * 1000)
                                .add(userCountGroupedByStartTime.get(key)))
                );

        writeVariableAsFile(reportsDirectory, "userActivity",
                series.build()
        );

    }

    private List<Sample> getRidOfOutliers(List<Sample> samples) {
        List<Long> times = samples.stream().map(s -> s.getEnd() - s.getStart()).sorted().collect(toList());

        double percentile1 = interpolateList(1, times);
        double percentile99 = interpolateList(99, times);

        return samples.stream().filter(s -> {
            long time = s.getEnd() - s.getStart();
            return percentile1 < time && time < percentile99;
        }).collect(toList());

    }

    private void generateResponseTimeDistributionsData(File reportsDirectory, List<Sample> allSamples, List<String> names) throws Exception {

        List<Sample> samples = getRidOfOutliers(allSamples);
        List<Long> times = samples.stream().map(s -> s.getEnd() - s.getStart()).collect(toList());
        Long max = times.stream().max(Comparator.<Long>naturalOrder()).get();
        Long min = times.stream().min(Comparator.<Long>naturalOrder()).get();

        JsonArrayBuilder categories = builderFactory.createArrayBuilder();
        range(0, (int) (max - min) / 10).forEach(i -> categories.add("" + (i * 10) + "ms"));

        JsonArrayBuilder distributions = builderFactory.createArrayBuilder();


        names.forEach(name -> {

            List<Long> specificTimes = allSamples.stream().filter(s -> s.getOperationName().equals(name)).map(s -> s.getEnd() - s.getStart()).collect(toList());

            JsonArrayBuilder values = builderFactory.createArrayBuilder();
            range(0, (int) (max - min) / 10).forEach(i -> {
                long count = specificTimes.stream().filter(j -> (i * 10 + min) <= j && ((i * 10) + 10 + min) > j).count();
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

    private void generateStatisticsData(File reportsDirectory, List<Sample> allSamples, List<String> names, long durationInSeconds) throws Exception {
        JsonArrayBuilder rows = builderFactory.createArrayBuilder();
        rows.add(generateStatisticsForRow("All", allSamples, durationInSeconds));
        names.forEach(name -> rows.add(generateStatisticsForRow(name, allSamples.stream().filter(s -> s.getOperationName().equals(name)).collect(toList()), durationInSeconds)));
        writeVariableAsFile(reportsDirectory, "statistics", rows.build());
    }

    private JsonArrayBuilder generateStatisticsForRow(String title, List<Sample> samples, long durationInSeconds) {


        List<Long> timeDistribution = new ArrayList<>();
        long totalTime = 0;
        long totalCount = 0;
        long good = 0;
        long bad = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (Sample sample : samples) {

            totalCount++;
            if (sample.isOk()) {
                good++;
            } else {
                bad++;
            }

            long time = sample.getEnd() - sample.getStart();
            timeDistribution.add(time);
            if (time < min) {
                min = time;
            }
            if (time > max) {
                max = time;
            }
            totalTime += time;

        }

        long mean = totalTime / totalCount;
        long sumOfSquares = 0;
        for (Sample sample : samples) {
            long time = sample.getEnd() - sample.getStart();
            sumOfSquares += (time - mean) * (time - mean);
        }
        double standardDeviation = Math.sqrt(((float) sumOfSquares / totalCount));
        double standardError = standardDeviation / Math.sqrt(totalCount);

        sort(timeDistribution);
        double percentile95 = interpolateList(95, timeDistribution);
        double percentile99 = interpolateList(99, timeDistribution);

        Map<Long, Long> summary = samples.stream().map(s -> s.getStart() / 1000).collect(groupingBy(o -> o, counting()));
        double totalRequests = summary.values().stream().mapToDouble(a -> a).sum();

        double requestsPerSecond = totalRequests/durationInSeconds;

        return builderFactory.createArrayBuilder()
                .add(title)
                .add(totalCount)
                .add(good)
                .add(bad)
                .add(decimalFormat.format((float) bad / totalCount))
                .add(min)
                .add(max)
                .add(mean)
                .add((long) standardError)
                .add((long) standardDeviation)
                .add(decimalFormat.format(percentile95))
                .add(decimalFormat.format(percentile99))
                .add(decimalFormat.format(requestsPerSecond));
    }

    private void generateIndicatorData(File reportsDirectory, List<Sample> allSamples) throws Exception {

        List<Long> allTimes = allSamples.stream().map(s -> s.getEnd() - s.getStart()).collect(toList());

        writeVariableAsFile(reportsDirectory,
                "indicatorData",
                builderFactory.createArrayBuilder()
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("t < 800ms")
                                        .add(allTimes.stream().filter(i -> i < 800).count())
                        )
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("800ms < t < 1200ms")
                                        .add(allTimes.stream().filter(i -> i >= 800 && i < 1200).count())
                        )
                        .add(
                                builderFactory.createArrayBuilder()
                                        .add("1200ms < t")
                                        .add(allTimes.stream().filter(i -> i >= 1200).count())

                        )
                        .build()
        );
    }

    private void generateRequestTypesData(File reportsDirectory, List<Sample> allSamples, List<String> names) throws Exception {
        JsonArrayBuilder data = builderFactory.createArrayBuilder();
        names.forEach(name ->
                data.add(builderFactory.createObjectBuilder()
                                .add("name", name)
                                .add("y", allSamples.stream().filter(s -> s.getOperationName().equals(name)).count())
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
            throw new RuntimeException("io error copying file " + fileName,e);
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
