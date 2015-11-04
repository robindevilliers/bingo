package uk.co.malbec.hound;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface Sampler {

    void addSample(String username, String operationName, DateTime start, DateTime end, String errorMessage, String detailedErrorMessage);

    Stream<Sample> stream() throws IOException;
}
