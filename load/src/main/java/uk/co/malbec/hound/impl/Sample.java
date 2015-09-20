package uk.co.malbec.hound.impl;


public class Sample {

    private String name;

    private long start;

    private long end;

    public Sample(String name, long start, long end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
