package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;


public class DifferenceConsumer implements Consumer<Long> {

    private boolean fromTo = false;
    private Reference<Long> longReference;
    private Consumer<Long> consumer;

    public DifferenceConsumer(Reference<Long> longReference, Consumer<Long> consumer) {
        this.longReference = longReference;
        this.consumer = consumer;
        this.fromTo = false;
    }

    public DifferenceConsumer(Consumer<Long> consumer, Reference<Long> longReference) {
        this.longReference = longReference;
        this.consumer = consumer;
        this.fromTo = true;
    }

    @Override
    public void accept(Long longValue) {
        if (fromTo) {
            consumer.accept(longValue - longReference.get());
        } else {
            consumer.accept(longReference.get() - longValue);
        }
    }
}
