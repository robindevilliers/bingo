package uk.co.malbec.hound.reporter;


import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.reporter.machinery.Machinery;
import uk.co.malbec.hound.reporter.machinery.Referenceable;

import java.util.function.Consumer;

import static java.util.function.Function.identity;
import static uk.co.malbec.hound.reporter.machinery.Machinery.*;
import static uk.co.malbec.hound.reporter.machinery.Machinery.reference;

public class FirstPassProcessor implements Consumer<Sample> {

    private Consumer<Sample> firstPass;

    public FirstPassProcessor(Data data){

        this.firstPass = all(
                (Consumer<Sample>) updateScalarIf(Sample::getStart, Machinery::lessThan, data.getEarliestStartTime()),
                (Consumer<Sample>) updateScalarIf(Sample::getStart, Machinery::greaterThan, data.getLatestStartTime()),
                (Consumer<Sample>) partitionBy(HtmlReporter::time, Machinery::lessThan, data.getIndicatorCategories(), count(reference(data.getIndicatorCategories()))),
                (Consumer<Sample>) partitionBy(Sample::getOperationName, String::equals, data.getOperationNameCategories(), statisticsSet(data.getOperationNameCategories())),
                (Consumer<Sample>) statisticsSet(referenceable(data.getStatistics())),
                (Consumer<Sample>) filter(s -> !s.isOk(),
                        partitionBy(Sample::getErrorMessage, String::equals, data.getErrorMessageCategories(),
                                all(
                                        count(reference(data.getErrorMessageCategories(), ErrorSummary::getTotalCount)),
                                        limit(100, reference(data.getErrorMessageCategories(), ErrorSummary::getErrors))
                                )
                        )
                ),
                (Consumer<Sample>) partitionBy(HtmlReporter::startSecond, Long::equals, data.getTimeDistributionCategories(),
                        partitionBy(Sample::getUsername, String::equals, reference(data.getTimeDistributionCategories()),
                                count(referenceOfGroup(reference(data.getTimeDistributionCategories())))
                        )
                )
        );
    }

    @Override
    public void accept(Sample sample) {
        firstPass.accept(sample);
    }

    public static Consumer<Sample> statisticsSet(Referenceable<Statistics> referenceable) {
        return all(
                count(reference(referenceable, Statistics::getTotalCount)),
                filter(Sample::isOk, count(reference(referenceable, Statistics::getGood))),
                filter(s -> !s.isOk(), count(reference(referenceable, Statistics::getBad))),
                Machinery.map(HtmlReporter::time, limit(2000000, reference(referenceable, Statistics::getTimeDistribution))),
                Machinery.map(HtmlReporter::time, updateScalarIf(identity(), Machinery::lessThan, reference(referenceable, Statistics::getMin))),
                Machinery.map(HtmlReporter::time, updateScalarIf(identity(), Machinery::greaterThan, reference(referenceable, Statistics::getMax))),
                Machinery.map(HtmlReporter::time, sum(reference(referenceable, Statistics::getTotalTime)))
        );
    }
}
