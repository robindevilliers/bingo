package uk.co.malbec.hound.reporter.machinery;


import java.util.function.Consumer;

public class Scalar<T> implements Consumer<T> {

    private T value;

    public Scalar(T t){
        this.value = t;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public void accept(T value) {
        this.value = value;
    }
}
