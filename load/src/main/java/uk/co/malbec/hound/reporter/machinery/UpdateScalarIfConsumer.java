package uk.co.malbec.hound.reporter.machinery;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;


public class UpdateScalarIfConsumer<T, U> implements Consumer<U> {

    private BiPredicate<T, T> acceptor;

    private Function<U, T> accessor;

    private Reference<Scalar<T>> scalarReference;

    public UpdateScalarIfConsumer(BiPredicate<T, T> acceptor, Function<U, T> accessor, Reference<Scalar<T>> scalarReference) {
        this.acceptor = acceptor;
        this.accessor = accessor;
        this.scalarReference = scalarReference;
    }

    @Override
    public void accept(U u) {
        T v = accessor.apply(u);
        if (scalarReference.get().getValue() == null){
            scalarReference.get().setValue(v);
        } else if (acceptor.test(v, scalarReference.get().getValue())) {
            scalarReference.get().setValue(v);
        }
    }
}
