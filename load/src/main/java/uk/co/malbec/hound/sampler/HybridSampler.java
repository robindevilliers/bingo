package uk.co.malbec.hound.sampler;

import org.joda.time.DateTime;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.Sampler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
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

    private List[] collectors;
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

    private int threadSize;

    public HybridSampler(int threadSize, File workingDir) {
        this.threadSize = threadSize;
        collectors = new List[threadSize];
        dataDir = new File(workingDir, "data");

        if (!dataDir.isDirectory()) {
            dataDir.mkdirs();
        }
        try {
            fileWriter = new BufferedWriter(new PrintWriter(new File(dataDir, "sample.data")));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        range(0, threadSize).forEach(i -> collectors[i] = new ArrayList<>());

        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage) {
        collectors[threadId.get() % threadSize].add(new Sample(errorMessage == null, username, operationName, start.getMillis(), end.getMillis(), errorMessage));
    }


    @Override
    public List<Sample> getAllSamples() {
        while (true) {
            synchronized (collectors) {
                boolean stillHasSamples = false;
                for (int i = 0; i < threadSize; i++) {
                    if (!collectors[i].isEmpty()) {
                        stillHasSamples = true;
                    }
                }
                if (!stillHasSamples) {
                    break;
                }
            }
            pause(500);
        }

        List<Sample> allSamples = new ArrayList<>();

        try {
            synchronized (fileWriter) {
                fileWriter.close();
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataDir, "sample.data"))));
            String line = bufferedReader.readLine();
            while (line != null) {

                String[] tokens = line.split(",");
                boolean error = tokens[0].equals("1");
                String username = tokens[1];
                String operationName = tokens[2];
                long start = Long.parseLong(tokens[3]);
                long end = Long.parseLong(tokens[4]);
                String errorMessage = tokens.length == 6 ? tokens[5] : null;
                allSamples.add(new Sample(error, username, operationName, start, end, errorMessage));
                line = bufferedReader.readLine();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return allSamples;
    }


    @Override
    public void run() {

        while (true) {
            range(0, threadSize).forEach(i -> {

                List samples = null;
                synchronized (fileWriter) {
                    synchronized (collectors) {

                        if (!collectors[i].isEmpty()) {
                            samples = collectors[i];
                            collectors[i] = new ArrayList();
                        }
                    }

                    if (samples != null) {

                        samples.stream().forEach(o -> {
                            Sample sample = (Sample) o;
                            try {
                                fileWriter.write(format("%1d,%s, %s,%d,%d,%s%n", sample.isOk() ? 1 : 0, sample.getUsername(), sample.getOperationName(), sample.getStart(), sample.getEnd(), sample.isOk() ? "" : sample.getErrorMessage()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                }
            });

            pause(500);
        }

    }
}
