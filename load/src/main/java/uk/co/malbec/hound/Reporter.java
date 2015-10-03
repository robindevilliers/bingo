package uk.co.malbec.hound;


import org.joda.time.DateTime;

import java.util.List;

public interface Reporter {

    public void generate(List<Sample> allSamples);
}
