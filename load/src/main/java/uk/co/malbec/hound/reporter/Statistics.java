package uk.co.malbec.hound.reporter;

import uk.co.malbec.hound.reporter.machinery.Scalar;
import uk.co.malbec.hound.reporter.machinery.Vector;

import static uk.co.malbec.hound.reporter.machinery.Machinery.scalar;
import static uk.co.malbec.hound.reporter.machinery.Machinery.vector;

public class Statistics {

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
