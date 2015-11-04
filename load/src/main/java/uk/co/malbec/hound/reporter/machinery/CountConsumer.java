package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class CountConsumer<T>  implements Consumer<T> {

    private Reference<Scalar<Long>> scalarReference;

    public CountConsumer(Reference<Scalar<Long>> scalarReference ){
        this.scalarReference = scalarReference;
    }

    @Override
    public void accept(T t) {
        scalarReference.get().setValue(scalarReference.get().getValue() + 1);
    }
}
