package uk.co.malbec.hound;

import org.joda.time.DateTime;

import java.util.List;

public interface Sampler {

    void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage);

    List<Sample> getAllSamples();
}
