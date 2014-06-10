package com.pestcontrolenterprise.util;

import com.google.common.collect.ImmutableList;
import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author myzone
 * @date 5/13/14
 */
public class HibernateStream<T> implements Stream<T> {

    protected final Function<Function<Criteria, Criteria>, List<T>> resolver;

    protected final List<HibernatePredicate<? super T>> hibernatePredicates;
    protected final List<Predicate<? super T>> nativePredicates;

    public HibernateStream(Function<Function<Criteria, Criteria>, List<T>> resolver) {
        this.resolver = resolver;

        hibernatePredicates = new ArrayList<>();
        nativePredicates = new ArrayList<>();
    }

    public HibernateStream(Function<Function<Criteria, Criteria>, List<T>> resolver, List<HibernatePredicate<? super T>> hibernatePredicates, List<Predicate<? super T>> nativePredicates) {
        this.resolver = resolver;
        this.hibernatePredicates = hibernatePredicates;
        this.nativePredicates = nativePredicates;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Stream<T> filter(Predicate<? super T> predicate) {
        if (predicate instanceof HibernatePredicate) {
            List<HibernatePredicate<? super T>> hibernatePredicates = new ArrayList<>(this.hibernatePredicates);

            hibernatePredicates.add((HibernatePredicate<? super T>) predicate);

            return new HibernateStream<>(resolver, hibernatePredicates, nativePredicates);
        } else {
            List<Predicate<? super T>> nativePredicates = new ArrayList<>(this.nativePredicates);

            nativePredicates.add(predicate);

            return new HibernateStream<>(resolver, hibernatePredicates, nativePredicates);
        }
    }

    @Override
    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
        return endHack().map(mapper);
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return endHack().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return endHack().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return endHack().mapToDouble(mapper);
    }

    @Override
    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return endHack().flatMap(mapper);
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return endHack().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return endHack().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return endHack().flatMapToDouble(mapper);
    }

    @Override
    public Stream<T> distinct() {
        return endHack().distinct();
    }

    @Override
    public Stream<T> sorted() {
        return endHack().sorted();
    }

    @Override
    public Stream<T> sorted(Comparator<? super T> comparator) {
        return endHack().sorted(comparator);
    }

    @Override
    public Stream<T> peek(Consumer<? super T> action) {
        return endHack().peek(action);
    }

    @Override
    public Stream<T> limit(long maxSize) {
        return endHack().limit(maxSize);
    }

    @Override
    public Stream<T> skip(long n) {
        return endHack().skip(n);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        endHack().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        endHack().forEachOrdered(action);
    }

    @Override
    public Object[] toArray() {
        return endHack().toArray();
    }

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return endHack().toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return endHack().reduce(identity, accumulator);
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return endHack().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return endHack().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return endHack().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return endHack().collect(collector);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return endHack().min(comparator);
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return endHack().max(comparator);
    }

    @Override
    public long count() {
        return endHack().count();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return endHack().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return endHack().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return endHack().noneMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        return endHack().findFirst();
    }

    @Override
    public Optional<T> findAny() {
        return endHack().findAny();
    }

    @Override
    public Iterator<T> iterator() {
        return endHack().iterator();
    }

    @Override
    public Spliterator<T> spliterator() {
        return endHack().spliterator();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public Stream<T> sequential() {
        return this;
    }

    @Override
    public Stream<T> parallel() {
        return endHack().parallel();
    }

    @Override
    public Stream<T> unordered() {
        return endHack().unordered();
    }

    @Override
    public Stream<T> onClose(Runnable closeHandler) {
        return endHack().onClose(closeHandler);
    }

    @Override
    public void close() {
    }

    @SuppressWarnings("unchecked")
    protected Stream<T> endHack() {
        Stream<T> result = resolver.apply(criteria -> {
            for (HibernatePredicate<? super T> hibernatePredicate : hibernatePredicates) {
                criteria = hibernatePredicate.describeItself(criteria);
            }

            return criteria;
        }).stream();

        for (Predicate<? super T> predicate : nativePredicates) {
            result = result.filter(predicate);
        }

        return result;
    }

    public interface HibernatePredicate<T> extends Predicate<T> {

        Criteria describeItself(Criteria criteria);

    }

}
