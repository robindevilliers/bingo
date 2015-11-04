package uk.co.malbec.hound.reporter;


import uk.co.malbec.hound.reporter.machinery.CategoryGroup;
import uk.co.malbec.hound.reporter.machinery.Scalar;

import static uk.co.malbec.hound.reporter.machinery.Machinery.*;

public class Data {

    private Scalar<Long> earliestStartTime = scalar(Long.MAX_VALUE);
    private Scalar<Long> latestStartTime = scalar(0L);
    private CategoryGroup<Long, Scalar<Long>> indicatorCategories = literalCategorization(() -> new Scalar<>(0L), 800L, 1200L, Long.MAX_VALUE);
    private CategoryGroup<String, Statistics> operationNameCategories = dynamicCategorization(Statistics::new);
    private Statistics statistics = new Statistics();
    private CategoryGroup<String, ErrorSummary> errorMessageCategories = dynamicCategorization(ErrorSummary::new);
    private CategoryGroup<Long, CategoryGroup<String, Scalar<Long>>> timeDistributionCategories = dynamicCategorization(() -> dynamicCategorization(() -> new Scalar<>(0L)));

    public Scalar<Long> getEarliestStartTime() {
        return earliestStartTime;
    }

    public Scalar<Long> getLatestStartTime() {
        return latestStartTime;
    }

    public CategoryGroup<Long, Scalar<Long>> getIndicatorCategories() {
        return indicatorCategories;
    }

    public CategoryGroup<String, Statistics> getOperationNameCategories() {
        return operationNameCategories;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public CategoryGroup<String, ErrorSummary> getErrorMessageCategories() {
        return errorMessageCategories;
    }

    public CategoryGroup<Long, CategoryGroup<String, Scalar<Long>>> getTimeDistributionCategories() {
        return timeDistributionCategories;
    }
}
