package uk.co.malbec.hound.sampler;


import org.joda.time.DateTime;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.Sampler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;


/*
Here is the performance characteristics of this sampler running bingo for 3 minutes.

mean     std-dev   count  std-err
0.00047  0.02158   437904 0.00003261
0.00049  0.02254   379814 0.00003657
0.00050  0.02262   437605 0.00003419

The mean value is the value that the sampler is likely to have on the test by consuming thread time.
This value is not included in the sample measurements of operations.

The above times are acceptable.  We don't have persistence, so this will be a problem for long running tests.
*/
public class BucketedInMemorySampler implements Sampler {

    private List<Sample>[] collectors = new List[100];

    private static final AtomicInteger nextId = new AtomicInteger(0);

    private static final ThreadLocal<Integer> threadId =
            new ThreadLocal<Integer>() {
                @Override
                protected Integer initialValue() {
                    return nextId.getAndIncrement();
                }
            };


    public BucketedInMemorySampler() {
        range(0, 100).forEach(i -> collectors[i] = new ArrayList<>());
    }

    @Override
    public void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage) {
        collectors[threadId.get() % 100].add(new Sample(errorMessage == null,username, operationName, start.getMillis(), end.getMillis(), errorMessage));
    }

    @Override
    public List<Sample> getAllSamples() {
        return stream(collectors).flatMap(list -> list.stream()).collect(toList());
    }
}
