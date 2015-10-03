package uk.co.malbec.hound.sampler;


import org.joda.time.DateTime;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.Sampler;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Long.parseLong;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;


/*
Here is the performance characteristics of this sampler running bingo for 3 minutes.

mean     std-dev   count  std-err
0.02918  1.67493   381008 0.00271349
0.06710  4.83693   444959 0.00725120
0.02175  1.25360   429439 0.00191297

The mean value is the value that the sampler is likely to have on the test by consuming thread time.
This value is not included in the sample measurements of operations.

The above times are acceptable.  And we have persistence with this sampler, so you can run it without memory issues.

If the above times where included in the sample measurement, they would be significant. But they are not, so no
need to worry.  Its still probably nicer to use the HybridSampler.

This sampler will consume processing time from the limited thread pool, so it may delay the timing of the script
if the load is on the bleeding edge (ie extreme) and socket io is so bad, its starving the thread pool.  But if that
were the case, the bad performance would be obvious, so accuracy is slightly less relevant.

*/

public class FileStoreSampler implements Sampler {

    private BufferedWriter[] collectors = new BufferedWriter[100];

    private static final AtomicInteger nextId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadId =
            new ThreadLocal<Integer>() {
                @Override
                protected Integer initialValue() {
                    return nextId.getAndIncrement();
                }
            };

    private File dataDir;

    public FileStoreSampler(File workingDir) {
        dataDir = new File(workingDir, "data");

        if (!dataDir.isDirectory()){
            dataDir.mkdirs();
        }

        range(0, 100).forEach(i -> {
            try {
                collectors[i] = new BufferedWriter(new PrintWriter(new File(dataDir,format("sample-%s.data", i))));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Override
    public void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage) {

        BufferedWriter writer = collectors[threadId.get() % 100];
        try {
            writer.write(format("%1d,%s,%s,%d,%d,%s%n", errorMessage != null ? 1 : 0, username, operationName, start.getMillis(), end.getMillis(), errorMessage == null ? "" : errorMessage));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public List<Sample> getAllSamples() {
        List<Sample> allSamples = new ArrayList<>();
        range(0, 100).forEach(i -> {
            try {
                collectors[i].close();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(dataDir,format("sample-%s.data", i)))));
                String line = bufferedReader.readLine();
                while (line != null){
                    String[] tokens = line.split(",");
                    boolean error = tokens[0].equals("1");
                    String username = tokens[1];
                    String operationName = tokens[2];
                    long start = parseLong(tokens[3]);
                    long end = parseLong(tokens[4]);
                    String errorMessage = tokens.length == 6 ? tokens[5]: null;
                    allSamples.add(new Sample(error, username, operationName, start, end, errorMessage));
                    line = bufferedReader.readLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return allSamples;
    }

}
