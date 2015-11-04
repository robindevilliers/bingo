package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;

public class PassConsumer<T> implements Consumer<T> {

    private Reference<Consumer<T>> consumerReference;

    public PassConsumer(Reference<Consumer<T>> consumerReference) {
        this.consumerReference = consumerReference;
    }

    @Override
    public void accept(T t) {
        consumerReference.get().accept(t);
    }
}
