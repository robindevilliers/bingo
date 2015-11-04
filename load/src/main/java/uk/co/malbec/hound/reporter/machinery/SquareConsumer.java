package uk.co.malbec.hound.reporter.machinery;

import java.util.function.Consumer;

public class SquareConsumer implements Consumer<Long> {

    private Consumer<Long> consumer;

    public SquareConsumer(Consumer<Long> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void accept(Long l) {
        consumer.accept(l * l);
    }
}
