package uk.co.malbec.hound.reporter;

import uk.co.malbec.hound.Sample;
import uk.co.malbec.hound.reporter.machinery.Scalar;
import uk.co.malbec.hound.reporter.machinery.Vector;

import static uk.co.malbec.hound.reporter.machinery.Machinery.scalar;
import static uk.co.malbec.hound.reporter.machinery.Machinery.vector;

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
