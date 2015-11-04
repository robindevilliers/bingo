package uk.co.malbec.hound.reporter;

import org.junit.Test;
import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.reporter.machinery.*;

import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.Collections.sort;
import static java.util.function.Function.identity;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static uk.co.malbec.hound.Utils.interpolateList;
import static uk.co.malbec.hound.reporter.machinery.Machinery.*;
import static uk.co.malbec.hound.reporter.machinery.Machinery.count;
import static uk.co.malbec.hound.reporter.machinery.Machinery.map;

public class MachineryTest {


    public static class Statistics {

        private Scalar<Long> totalCount = scalar(0L);
        private Scalar<Long> totalTime = scalar(0L);
        private Scalar<Long> good = scalar(0L);
        private Scalar<Long> bad = scalar(0L);
        private Scalar<Long> min = scalar(Long.MAX_VALUE);
        private Scalar<Long> max = scalar(0L);
        private Vector<Long> timeDistribution = vector();
        private Scalar<Long> sumOfSquares = scalar(0L);
        private Scalar<Long> minimumTime = scalar(0L);
        private Scalar<Long> maximumTime = scalar(0L);

        private Vector<Long> timeDistributionExcludingOutliers = vector();

        public Scalar<Long> getTotalTime() {
            return totalTime;
        }

        public Scalar<Long> getTotalCount() {
            return totalCount;
        }

        public Scalar<Long> getGood() {
            return good;
        }

        public Scalar<Long> getBad() {
            return bad;
        }

        public Scalar<Long> getMin() {
            return min;
        }

        public Scalar<Long> getMax() {
            return max;
        }

        public Vector<Long> getTimeDistribution() {
            return timeDistribution;
        }

        public Scalar<Long> getSumOfSquares() {
            return sumOfSquares;
        }

        public Scalar<Long> getMinimumTime() {
            return minimumTime;
        }

        public Scalar<Long> getMaximumTime() {
            return maximumTime;
        }

        public Vector<Long> getTimeDistributionExcludingOutliers() {
            return timeDistributionExcludingOutliers;
        }

        public long getMean() {
            return totalTime.getValue() / totalCount.getValue();
        }

        public double getStandardDeviation() {
            return Math.sqrt(((float) sumOfSquares.getValue() / totalCount.getValue()));
        }

        public double getStandardError() {
            return getStandardDeviation() / Math.sqrt(totalCount.getValue());
        }
    }

    public class ErrorSummary {
        private Scalar<Long> totalCount = scalar(0L);
        private Vector<Sample> errors = vector();

        public Scalar<Long> getTotalCount() {
            return totalCount;
        }

        public Vector<Sample> getErrors() {
            return errors;
        }
    }

    public static Consumer<Sample> statisticsSet(Referenceable<Statistics> referenceable) {
        return all(
                count(reference(referenceable, Statistics::getTotalCount)),
                filter(Sample::isOk, count(reference(referenceable, Statistics::getGood))),
                filter(s -> !s.isOk(), count(reference(referenceable, Statistics::getBad))),
                map(MachineryTest::time, reference(referenceable, Statistics::getTimeDistribution)),
                map(MachineryTest::time, updateScalarIf(identity(), Machinery::lessThan, reference(referenceable, Statistics::getMin))),
                map(MachineryTest::time, updateScalarIf(identity(), Machinery::greaterThan, reference(referenceable, Statistics::getMax))),
                map(MachineryTest::time, sum(reference(referenceable, Statistics::getTotalTime)))
        );
    }

    @Test
    public void test() throws Exception {

        //Given

        Scalar<Long> earliestStartTime = scalar(Long.MAX_VALUE);
        Scalar<Long> latestStartTime = scalar(0L);
        CategoryGroup<Long, Scalar<Long>> indicatorCategories = literalCategorization(() -> new Scalar<>(0L), 800L, 1200L, Long.MAX_VALUE);
        CategoryGroup<String, Statistics> operationNameCategories = dynamicCategorization(Statistics::new);
        Statistics statistics = new Statistics();
        CategoryGroup<String, ErrorSummary> errorMessageCategories = dynamicCategorization(ErrorSummary::new);

        Consumer<Sample> firstPass = all(
                (Consumer<Sample>) updateScalarIf(Sample::getStart, Machinery::lessThan, earliestStartTime),
                (Consumer<Sample>) updateScalarIf(Sample::getStart, Machinery::greaterThan, latestStartTime),
                (Consumer<Sample>) partitionBy(MachineryTest::time, Machinery::lessThan, indicatorCategories, count(reference(indicatorCategories))),
                (Consumer<Sample>) partitionBy(Sample::getOperationName, String::equals, operationNameCategories, statisticsSet(operationNameCategories)),
                (Consumer<Sample>) statisticsSet(referenceable(statistics)),
                (Consumer<Sample>) filter(s -> !s.isOk(),
                        partitionBy(Sample::getErrorMessage, String::equals, errorMessageCategories,
                                all(
                                        count(reference(errorMessageCategories, ErrorSummary::getTotalCount)),
                                        limit(100, reference(errorMessageCategories, ErrorSummary::getErrors))
                                )
                        )
                )
        );


        //When
        firstPass.accept(new Sample(true, "user14", "REGISTER", 1445730762017L, 1445730762052L, null, null));
        firstPass.accept(new Sample(true, "user14", "CHAT", 1445730762084L, 1445730763122L, null, null));
        firstPass.accept(new Sample(true, "user47", "REGISTER", 1445730762130L, 1445730762160L, null, null));
        firstPass.accept(new Sample(true, "user19", "REGISTER", 1445730761935L, 1445730764052L, null, null));
        firstPass.accept(new Sample(false, "user47", "CHAT", 1445730761875L, 1445730762052L, "your auntie", "this is an auntie message"));
        firstPass.accept(new Sample(true, "user14", "REGISTER", 1445730762085L, 1445730763116L, null, null));
        firstPass.accept(new Sample(false, "user19", "REGISTER", 1445730761821L, 1445730762052L, "bob's your uncle", "this is a bob message"));

        sort(statistics.getTimeDistribution().getValues());
        double percentile1 = interpolateList(1, statistics.getTimeDistribution().getValues());
        double percentile99 = interpolateList(99, statistics.getTimeDistribution().getValues());


        Consumer<Sample> secondPass = all(
                partitionBy(Sample::getOperationName, String::equals, operationNameCategories,
                        (Consumer<Sample>) map(MachineryTest::time,
                                all(
                                        difference(reference(operationNameCategories, Statistics::getMean), square(sum(reference(operationNameCategories, Statistics::getSumOfSquares)))),
                                        filter(l -> l > percentile1 && l < percentile99,
                                                all(
                                                        updateScalarIf(identity(), Machinery::lessThan, reference(operationNameCategories, Statistics::getMinimumTime)),
                                                        updateScalarIf(identity(), Machinery::greaterThan, reference(operationNameCategories, Statistics::getMaximumTime)),
                                                        pass(reference(operationNameCategories, Statistics::getTimeDistributionExcludingOutliers))
                                                )
                                        )
                                )
                        )
                ),
                (Consumer<Sample>) map(MachineryTest::time, difference(statistics.getMean(), square(sum(reference(referenceable(statistics), Statistics::getSumOfSquares)))))
        );


        secondPass.accept(new Sample(true, "user14", "REGISTER", 1445730762017L, 1445730762052L, null, null));
        secondPass.accept(new Sample(true, "user14", "CHAT", 1445730762084L, 1445730763122L, null, null));
        secondPass.accept(new Sample(true, "user47", "REGISTER", 1445730762130L, 1445730762160L, null, null));
        secondPass.accept(new Sample(true, "user19", "REGISTER", 1445730761935L, 1445730764052L, null, null));
        secondPass.accept(new Sample(false, "user47", "CHAT", 1445730761875L, 1445730762052L, "your auntie", "this is an auntie message"));
        secondPass.accept(new Sample(true, "user14", "REGISTER", 1445730762085L, 1445730763116L, null, null));
        secondPass.accept(new Sample(false, "user19", "REGISTER", 1445730761821L, 1445730762052L, "bob's your uncle", "this is a bob message"));


//        ResponseTimeDistributionsSampleConsumer responseTimeDistributionsSampleConsumer = new ResponseTimeDistributionsSampleConsumer(percentile1, percentile99);


        //Then
        assertThat(earliestStartTime.getValue(), is(1445730761821L));
        assertThat(latestStartTime.getValue(), is(1445730762130L));
        assertThat(indicatorCategories.get(800L).getValue(), is(4L));
        assertThat(indicatorCategories.get(1200L).getValue(), is(2L));
        assertThat(indicatorCategories.get(Long.MAX_VALUE).getValue(), is(1L));

        assertThat(operationNameCategories.get("REGISTER").getTotalCount().getValue(), is(5L));
        assertThat(operationNameCategories.get("REGISTER").getGood().getValue(), is(4L));
        assertThat(operationNameCategories.get("REGISTER").getBad().getValue(), is(1L));
        assertThat(operationNameCategories.get("REGISTER").getTimeDistribution().getValues().size(), is(5));
        assertThat(operationNameCategories.get("REGISTER").getMin().getValue(), is(30L));
        assertThat(operationNameCategories.get("REGISTER").getMax().getValue(), is(2117L));
        assertThat(operationNameCategories.get("REGISTER").getTotalTime().getValue(), is(3444L));

        assertThat(operationNameCategories.get("CHAT").getTotalCount().getValue(), is(2L));
        assertThat(operationNameCategories.get("CHAT").getGood().getValue(), is(1L));
        assertThat(operationNameCategories.get("CHAT").getBad().getValue(), is(1L));
        assertThat(operationNameCategories.get("CHAT").getTimeDistribution().getValues().size(), is(2));
        assertThat(operationNameCategories.get("CHAT").getMin().getValue(), is(177L));
        assertThat(operationNameCategories.get("CHAT").getMax().getValue(), is(1038L));
        assertThat(operationNameCategories.get("CHAT").getTotalTime().getValue(), is(1215L));

        assertThat(statistics.getTotalCount().getValue(), is(7L));
        assertThat(statistics.getGood().getValue(), is(5L));
        assertThat(statistics.getBad().getValue(), is(2L));
        assertThat(statistics.getTimeDistribution().getValues().size(), is(7));
        assertThat(statistics.getMin().getValue(), is(30L));
        assertThat(statistics.getMax().getValue(), is(2117L));
        assertThat(statistics.getTotalTime().getValue(), is(4659L));

        assertThat(errorMessageCategories.get("bob's your uncle").getTotalCount().getValue(), is(1L));
        assertThat(errorMessageCategories.get("your auntie").getTotalCount().getValue(), is(1L));

        assertThat(errorMessageCategories.get("bob's your uncle").getErrors().getValues().get(0).getDetailedErrorMessage(), is("this is a bob message"));
        assertThat(errorMessageCategories.get("your auntie").getErrors().getValues().get(0).getDetailedErrorMessage(), is("this is an auntie message"));


        assertThat(statistics.getStandardDeviation(), is(717.9349291544464));
        assertThat(statistics.getStandardError(), is(271.3538971527772));



        /*CategoryGroup<String, CategoryGroup<String, ScalarReference<Long>>> table = literalCategorization(
                () -> literalCategorization(
                        () -> new Scalar<>(0L)
                        , "REGISTER",
                        "CHAT"
                ),
                "user14",
                "user47",
                "user19"
        );*/

        /*Consumer<Sample> s4 = partitionBy(Sample::getUsername, Machinery::equals, table,
                partitionBy(Sample::getOperationName, String::equals, reference(table), count(reference(reference(table))))
        );*/

        /*assertThat(table.get("user14").get("REGISTER").getValue(), is(2L));
        assertThat(table.get("user14").get("CHAT").getValue(), is(1L));*/

    }


    public static Long time(Sample sample) {
        return sample.getEnd() - sample.getStart();
    }


}