package uk.co.malbec.hound.reporter.machinery;


import java.util.function.Consumer;

public class ScalarConsumer<T> implements Consumer<T> {

    private T value;
    @Override
    public void accept(T t) {
        value = t;
    }

    public T getValue() {
        return value;
    }
}
