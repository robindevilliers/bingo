package uk.co.malbec.hound.reporter.machinery;

import java.util.function.*;

import static java.util.Arrays.asList;

public class Machinery {

    //You will probably be wondering why, when we have a varargs version of the all() function... basically, the intellij syntax checker just isn't up to the task.

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5, Consumer<T> consumer6, Consumer<T> consumer7, Consumer<T> consumer8, Consumer<T> consumer9, Consumer<T> consumer10) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5, consumer6, consumer7, consumer8, consumer9, consumer10));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5, Consumer<T> consumer6, Consumer<T> consumer7, Consumer<T> consumer8, Consumer<T> consumer9) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5, consumer6, consumer7, consumer8, consumer9));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5, Consumer<T> consumer6, Consumer<T> consumer7, Consumer<T> consumer8) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5, consumer6, consumer7, consumer8));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5, Consumer<T> consumer6, Consumer<T> consumer7) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5, consumer6, consumer7));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5, Consumer<T> consumer6) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5, consumer6));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4, Consumer<T> consumer5) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4, consumer5));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3, Consumer<T> consumer4) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3, consumer4));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2, Consumer<T> consumer3) {
        return new AllConsumer<T>(asList(consumer1, consumer2, consumer3));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1, Consumer<T> consumer2) {
        return new AllConsumer<T>(asList(consumer1, consumer2));
    }

    public static <T> Consumer<T> all(Consumer<T> consumer1) {
        return new AllConsumer<T>(asList(consumer1));
    }

    /*public static <T> Consumer<T> all(Consumer<T>... consumers) {
        return new AllConsumer<>(asList(consumers));
    }*/

    public static <T extends Number> boolean lessThan(T lhs, T rhs) {
        return rhs.doubleValue() - lhs.doubleValue() > 0;
    }

    public static <T extends Number> boolean greaterThan(T lhs, T rhs) {
        return rhs.doubleValue() - lhs.doubleValue() < 0;
    }

    public static <T extends Number> boolean equals(T lhs, T rhs) {
        return rhs.doubleValue() - lhs.doubleValue() == 0;
    }

    public static <T> Scalar<T> scalar(T t) {
        return new Scalar<>(t);
    }

    public static <T> Vector<T> vector() {
        return new Vector<>();
    }

    public static <T, U> UpdateScalarIfConsumer<T, U> updateScalarIf(Function<U, T> accessor, BiPredicate<T, T> acceptor, Scalar<T> downstream) {
        return new UpdateScalarIfConsumer<T, U>(acceptor, accessor, () -> downstream);
    }

    public static <T, U> UpdateScalarIfConsumer<T, U> updateScalarIf(Function<U, T> accessor, BiPredicate<T, T> acceptor, Reference<Scalar<T>> downstreamReference) {
        return new UpdateScalarIfConsumer<T, U>(acceptor, accessor, downstreamReference);
    }

    public static <T, R, S> Consumer<T> partitionBy(Function<T, R> accessor, BiPredicate<R, R> acceptor, CategoryGroup<R, S> categoryGroup, Consumer<T> consumer) {
        return new PartitionByConsumer<>(accessor, acceptor, () -> categoryGroup, consumer);
    }

    public static <T, R, S> Consumer<T> partitionBy(Function<T, R> accessor, BiPredicate<R, R> acceptor, Reference<CategoryGroup<R, S>> categoryGroupReference, Consumer<T> consumer) {
        return new PartitionByConsumer<>(accessor, acceptor, categoryGroupReference, consumer);
    }

    public static <CATEGORY, COLLECTOR> LiteralCategoryGroup<CATEGORY, COLLECTOR> literalCategorization(Supplier<COLLECTOR> collectorSupplier, CATEGORY... breakpoints) {
        return new LiteralCategoryGroup<>(collectorSupplier, breakpoints);
    }

    public static <CATEGORY, COLLECTOR> DynamicCategoryGroup<CATEGORY, COLLECTOR> dynamicCategorization(Supplier<COLLECTOR> collectorSupplier) {
        return new DynamicCategoryGroup<>(collectorSupplier);
    }

    public static <T> Consumer<T> filter(Predicate<T> predicate, Consumer<T> consumer) {
        return new FilterConsumer<T>(predicate, consumer);
    }

    public static <T, R> Consumer<T> map(Function<T, R> mapper, Consumer<R> consumer) {
        return new MapConsumer<T, R>(mapper, () -> consumer);
    }

    public static <T, R> Consumer<T> map(Function<T, R> mapper, Reference<Consumer<R>> consumerReference) {
        return new MapConsumer<T, R>(mapper, consumerReference);
    }

    public static <T> Consumer<T> count(Reference<Scalar<Long>> scalarReference) {
        return new CountConsumer<T>(scalarReference);
    }

    public static <T> CountConsumer<T> count(Scalar<Long> scalar) {
        return new CountConsumer<T>(() -> scalar);
    }

    public static <T> Reference<T> reference(Referenceable<T> referenceable) {
        return referenceable::current;
    }

    public static <T, R> Reference<R> reference(Referenceable<T> referenceable, Function<T, R> function) {
        return () -> {
            return function.apply(referenceable.current());
        };
    }

    public static <T> Reference<T> reference(Reference<Referenceable<T>> reference) {
        return () -> reference.get().current();
    }

    public static <T, R> Reference<T> referenceOfGroup(Reference<CategoryGroup<R, T>> reference) {
        return () -> reference.get().current();
    }

    public static Consumer<Long> sum(Reference<Scalar<Long>> scalarReference) {
        return new SumConsumer(scalarReference);
    }

    public static Consumer<Long> sum(Scalar<Long> scalar) {
        return new SumConsumer(() -> scalar);
    }

    public static <T> Referenceable<T> referenceable(T t) {
        return () -> t;
    }

    public static <T> Consumer<T> limit(int limit, Consumer<T> consumer){
        return new LimitConsumer<>(limit, () -> consumer);
    }

    public static <T> Consumer<T> limit(int limit, Reference<Consumer<T>> consumerReference){
        return new LimitConsumer<>(limit, consumerReference);
    }

    public static  Consumer<Long> difference(Long l, Consumer<Long> consumer){
        return new DifferenceConsumer(() -> l, consumer);
    }

    public static  Consumer<Long> difference(Reference<Long> longReference, Consumer<Long> consumer){
        return new DifferenceConsumer(longReference, consumer);
    }

    public static  Consumer<Long> difference(Consumer<Long> consumer, Reference<Long> longReference){
        return new DifferenceConsumer(consumer, longReference);
    }

    public static  Consumer<Long> difference(Consumer<Long> consumer, Long l){
        return new DifferenceConsumer(consumer, () -> l);
    }

    public static Consumer<Long> square(Consumer<Long> consumer){
        return new SquareConsumer(consumer);
    }

    public static <T> Consumer<T> pass(Reference<Consumer<T>> consumerReference){
        return new PassConsumer<>(consumerReference);
    }
}
