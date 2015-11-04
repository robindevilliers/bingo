package uk.co.malbec.hound.reporter;

import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.reporter.machinery.Machinery;

import java.util.function.Consumer;

import static java.util.Collections.sort;
import static java.util.function.Function.identity;
import static uk.co.malbec.hound.Utils.interpolateList;
import static uk.co.malbec.hound.reporter.machinery.Machinery.*;
import static uk.co.malbec.hound.reporter.machinery.Machinery.updateScalarIf;

public class SecondPassProcessor implements Consumer<Sample> {

    private Consumer<Sample> secondPass;

    public SecondPassProcessor(Data data){

        sort(data.getStatistics().getTimeDistribution().getValues());
        double percentile1 = interpolateList(1, data.getStatistics().getTimeDistribution().getValues());
        double percentile99 = interpolateList(99, data.getStatistics().getTimeDistribution().getValues());

        secondPass = all(
                partitionBy(Sample::getOperationName, String::equals, data.getOperationNameCategories(),
                        (Consumer<Sample>) Machinery.map(HtmlReporter::time,
                                all(
                                        difference(reference(data.getOperationNameCategories(), Statistics::getMean), square(sum(reference(data.getOperationNameCategories(), Statistics::getSumOfSquares)))),
                                        filter(l -> l > percentile1 && l < percentile99,
                                                limit(2000000, reference(data.getOperationNameCategories(), Statistics::getTimeDistributionExcludingOutliers))
                                        )
                                )
                        )
                ),
                (Consumer<Sample>) Machinery.map(HtmlReporter::time, all(
                                difference(data.getStatistics().getMean(), square(sum(reference(referenceable(data.getStatistics()), Statistics::getSumOfSquares)))),
                                filter(l -> l > percentile1 && l < percentile99,
                                        all(
                                                updateScalarIf(identity(), Machinery::lessThan, data.getStatistics().getMinimumTime()),
                                                updateScalarIf(identity(), Machinery::greaterThan, data.getStatistics().getMaximumTime())
                                        )
                                )
                        )
                )
        );

    }

    @Override
    public void accept(Sample sample) {
        secondPass.accept(sample);
    }
}
