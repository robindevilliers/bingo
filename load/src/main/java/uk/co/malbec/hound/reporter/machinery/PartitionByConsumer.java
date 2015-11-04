package uk.co.malbec.hound.reporter.machinery;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;


public class PartitionByConsumer<T, R, B> implements Consumer<T> {

    private Function<T,B> accessor;
    private BiPredicate<B, B> acceptor;
    private Reference<CategoryGroup<B, R>> categoryGroupReference;
    private Consumer<T> consumer;

    public PartitionByConsumer(Function<T, B> accessor, BiPredicate<B, B> acceptor, Reference<CategoryGroup<B, R>> categoryGroupReference, Consumer<T> consumer) {
        this.accessor = accessor;
        this.acceptor = acceptor;
        this.categoryGroupReference = categoryGroupReference;
        this.consumer = consumer;
    }

    @Override
    public void accept(T t) {
        B value = accessor.apply(t);


        for (B key : categoryGroupReference.get().getKeys()){
            if (acceptor.test(value, key)){
                if (categoryGroupReference.get().apply(key)){
                    consumer.accept(t);
                }
                return;
            }
        }

        if (categoryGroupReference.get().apply(value)){
            consumer.accept(t);
        }
    }
}
