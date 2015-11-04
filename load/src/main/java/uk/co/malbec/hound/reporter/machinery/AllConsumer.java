package uk.co.malbec.hound.reporter.machinery;


import java.util.List;
import java.util.function.Consumer;

public class AllConsumer<T> implements Consumer<T> {

    private List<Consumer<T>> consumers;

    public AllConsumer(List<Consumer<T>> consumers){
        this.consumers = consumers;
    }

    @Override
    public void accept(T t) {
        for (Consumer<T> consumer : consumers){
            consumer.accept(t);
        }
    }
}
