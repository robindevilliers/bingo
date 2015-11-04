package uk.co.malbec.hound.sampler;

import org.joda.time.DateTime;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.Sampler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.synchronizedList;
import static java.util.stream.IntStream.range;
import static uk.co.malbec.hound.Utils.pause;

/*
Here is the performance characteristics of this sampler running bingo for 3 minutes.

mean     std-dev   count  std-err
0.00077  0.03069   372407 0.00005029
0.00063  0.02546   394548 0.00004054
0.00071  0.02698   394140 0.00004297

The mean value is the value that the sampler is likely to have on the test by consuming thread time.
This value is not included in the sample measurements of operations.

The above times are acceptable.  And we have persistence with this sampler, so you can run it without memory issues.
*/
public class HybridSampler implements Sampler, Runnable {

    private final List<List<Sample>> collectors = synchronizedList(new ArrayList<>());
    private BufferedWriter fileWriter;

    private static final AtomicInteger nextId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadId =
            new ThreadLocal<Integer>() {
                @Override
                protected Integer initialValue() {
                    return nextId.getAndIncrement();
                }
            };

    private File dataDir;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public HybridSampler setSampleDirectory(File dataDir) {
        if (!dataDir.isDirectory()) {
            dataDir.mkdirs();
        }
        this.dataDir = dataDir;
        try {
            fileWriter = new BufferedWriter(new PrintWriter(new File(this.dataDir, "sample.data")));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
        return this;
    }

    @Override
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage, String detailedErrorMessage) {
        Sample sample = new Sample(errorMessage == null, username, operationName, start.getMillis(), end.getMillis(), errorMessage, detailedErrorMessage);
        List<Sample> samples;
        synchronized (collectors) {


            while (collectors.size() <= threadId.get() + 1) {
                samples = new ArrayList<>();
                collectors.add(samples);
            }
            samples = collectors.get(threadId.get());
        }
        synchronized (samples) {
            samples.add(sample);
        }

    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public Stream<Sample> stream() throws IOException {
        while (true) {
            synchronized (collectors) {
                if (!collectors.stream().filter(samples -> !samples.isEmpty()).findAny().isPresent()) {
                    break;
                }
            }
            pause(500);
        }
        synchronized (fileWriter) {
            fileWriter.close();
        }

        Path path = Paths.get(dataDir.getAbsolutePath(), "sample.data");

        return Files.lines(path).map(line -> {
            String[] tokens = line.split(",");
            boolean error = tokens[0].equals("1");
            String username = tokens[1];
            String operationName = tokens[2];
            long start = Long.parseLong(tokens[3]);
            long end = Long.parseLong(tokens[4]);
            String errorMessage = null;
            String detailedErrorMessage = null;
            if (tokens.length >= 6) {
                errorMessage = new String(Base64.getDecoder().decode(tokens[5]));
            }
            if (tokens.length >= 7) {
                detailedErrorMessage = new String(Base64.getDecoder().decode(tokens[6]));
            }
            return new Sample(error,
                    username,
                    operationName,
                    start,
                    end,
                    errorMessage,
                    detailedErrorMessage
            );
        });
    }

    @Override
    @SuppressWarnings({"SynchronizeOnNonFinalField", "SynchronizationOnLocalVariableOrMethodParameter", "InfiniteLoopStatement"})
    public void run() {

        while (true) {

            range(0, collectors.size()).forEach(i -> {

                List<Sample> samples = null;
                synchronized (fileWriter) {
                    synchronized (collectors) {

                        if (!collectors.get(i).isEmpty()) {
                            samples = collectors.get(i);
                            collectors.set(i, new ArrayList<>());
                        }
                    }

                    if (samples != null) {
                        synchronized (samples) {
                            samples.stream().forEach(sample -> {
                                try {
                                    fileWriter.write(
                                            format("%1d,%s, %s,%d,%d,%s,%s%n",
                                                    sample.isOk() ? 1 : 0,
                                                    sample.getUsername(),
                                                    sample.getOperationName(),
                                                    sample.getStart(),
                                                    sample.getEnd(),
                                                    sample.isOk() ? "" : Base64.getEncoder().encodeToString(sample.getErrorMessage().getBytes()),
                                                    sample.getDetailedErrorMessage() == null ? "" : Base64.getEncoder().encodeToString(sample.getDetailedErrorMessage().getBytes())
                                            )
                                    );
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                }
            });

            pause(500);
        }

    }
}
