package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;

public class LimitConsumer<T> implements Consumer<T> {

    private int limit;

    private int count = 0;

    private Reference<Consumer<T>> consumerReference;

    public LimitConsumer(int limit, Reference<Consumer<T>> consumerReference) {
        this.limit = limit;
        this.consumerReference = consumerReference;
    }

    @Override
    public void accept(T t) {
        if (count < limit){
            count++;
            consumerReference.get().accept(t);
        }
    }
}
