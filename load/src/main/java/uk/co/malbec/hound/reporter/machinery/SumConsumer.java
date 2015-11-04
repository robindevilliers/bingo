package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;

public class SumConsumer implements Consumer<Long> {

    private Reference<Scalar<Long>> scalarReference;

    public SumConsumer(Reference<Scalar<Long>> scalarReference) {
        this.scalarReference = scalarReference;
    }

    @Override
    public void accept(Long value) {
        scalarReference.get().setValue(scalarReference.get().getValue() + value);
    }
}
