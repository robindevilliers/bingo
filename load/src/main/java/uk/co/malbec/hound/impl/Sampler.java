package uk.co.malbec.hound.impl;


import org.joda.time.DateTime;

import java.util.*;

import static java.util.Arrays.stream;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class Sampler {

    private List[] collectors = new List[100];


    public Sampler() {
        range(0, 100).forEach(i -> collectors[i] = synchronizedList(new ArrayList<>())); //TODO - think about what list to actually use.
    }

    public void addSample(String name, long threadId, DateTime start, DateTime end) {
        collectors[(int) (threadId % 100)].add(new Sample(name, start.getMillis(), end.getMillis()));
    }

    public void generateReport() {
        List<Sample> allSamples = (List<Sample>) stream(collectors).flatMap(list -> list.stream()).collect(toList());
        collectors = null;

        new ConsoleReporter().print(allSamples);
    }


}
