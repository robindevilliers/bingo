package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;
import java.util.function.Predicate;


public class FilterConsumer<T> implements Consumer<T> {

    private Predicate<T> predicate;
    private Consumer<T> consumer;

    public FilterConsumer(Predicate<T> predicate, Consumer<T> consumer) {
        this.predicate = predicate;
        this.consumer = consumer;
    }

    @Override
    public void accept(T t) {
        if (predicate.test(t)){
            consumer.accept(t);
        }
    }
}
