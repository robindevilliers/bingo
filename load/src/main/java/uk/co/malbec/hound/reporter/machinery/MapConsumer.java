package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;
import java.util.function.Function;

public class MapConsumer<T, R> implements Consumer<T> {

    private Function<T, R> mapper;
    private Reference<Consumer<R>> consumerReference;

    public MapConsumer(Function<T, R> mapper, Reference<Consumer<R>> consumerReference) {
        this.mapper = mapper;
        this.consumerReference = consumerReference;
    }

    @Override
    public void accept(T t) {
        consumerReference.get().accept(mapper.apply(t));
    }
}
