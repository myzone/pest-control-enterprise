package com.pestcontrolenterprise.util;

import com.google.common.base.Objects;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * myzone
 * 4/29/14
 */
public class RemoteStream<T> implements Stream<T> {

    private final String id;
    private final Stream<T> localStream;

    public RemoteStream(String id, Stream<T> localStream) {
        this.id = id;
        this.localStream = localStream;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoteStream)) return false;

        RemoteStream that = (RemoteStream) o;

        if (!id.equals(that.id)) return false;
        if (localStream != null ? !localStream.equals(that.localStream) : that.localStream != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("localStream", localStream)
                .toString();
    }

    public Stream<T> filter(Predicate<? super T> predicate) {return localStream.filter(predicate);}

    public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {return localStream.map(mapper);}

    public IntStream mapToInt(ToIntFunction<? super T> mapper) {return localStream.mapToInt(mapper);}

    public LongStream mapToLong(ToLongFunction<? super T> mapper) {return localStream.mapToLong(mapper);}

    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {return localStream.mapToDouble(mapper);}

    public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {return localStream.flatMap(mapper);}

    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {return localStream.flatMapToInt(mapper);}

    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {return localStream.flatMapToLong(mapper);}

    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {return localStream.flatMapToDouble(mapper);}

    @Override
    public Stream<T> distinct() {return localStream.distinct();}

    @Override
    public Stream<T> sorted() {return localStream.sorted();}

    public Stream<T> sorted(Comparator<? super T> comparator) {return localStream.sorted(comparator);}

    public Stream<T> peek(Consumer<? super T> action) {return localStream.peek(action);}

    @Override
    public Stream<T> limit(long maxSize) {return localStream.limit(maxSize);}

    @Override
    public Stream<T> skip(long n) {return localStream.skip(n);}

    public void forEach(Consumer<? super T> action) {localStream.forEach(action);}

    public void forEachOrdered(Consumer<? super T> action) {localStream.forEachOrdered(action);}

    @Override
    public Object[] toArray() {return localStream.toArray();}

    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {return localStream.toArray(generator);}

    public T reduce(T identity, BinaryOperator<T> accumulator) {return localStream.reduce(identity, accumulator);}

    public Optional<T> reduce(BinaryOperator<T> accumulator) {return localStream.reduce(accumulator);}

    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {return localStream.reduce(identity, accumulator, combiner);}

    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {return localStream.collect(supplier, accumulator, combiner);}

    public <R, A> R collect(Collector<? super T, A, R> collector) {return localStream.collect(collector);}

    public Optional<T> min(Comparator<? super T> comparator) {return localStream.min(comparator);}

    public Optional<T> max(Comparator<? super T> comparator) {return localStream.max(comparator);}

    @Override
    public long count() {return localStream.count();}

    public boolean anyMatch(Predicate<? super T> predicate) {return localStream.anyMatch(predicate);}

    public boolean allMatch(Predicate<? super T> predicate) {return localStream.allMatch(predicate);}

    public boolean noneMatch(Predicate<? super T> predicate) {return localStream.noneMatch(predicate);}

    @Override
    public Optional<T> findFirst() {return localStream.findFirst();}

    @Override
    public Optional<T> findAny() {return localStream.findAny();}

    public static <T1> Builder<T1> builder() {return Stream.builder();}

    public static <T1> Stream<T1> empty() {return Stream.empty();}

    public static <T1> Stream<T1> of(T1 t1) {return Stream.of(t1);}

    @SafeVarargs
    public static <T1> Stream<T1> of(T1... values) {return Stream.of(values);}

    public static <T1> Stream<T1> iterate(T1 seed, UnaryOperator<T1> f) {return Stream.iterate(seed, f);}

    public static <T1> Stream<T1> generate(Supplier<T1> s) {return Stream.generate(s);}

    public static <T1> Stream<T1> concat(Stream<? extends T1> a, Stream<? extends T1> b) {return Stream.concat(a, b);}

    @Override
    public Iterator<T> iterator() {return localStream.iterator();}

    @Override
    public Spliterator<T> spliterator() {return localStream.spliterator();}

    @Override
    public boolean isParallel() {return localStream.isParallel();}

    @Override
    public Stream<T> sequential() {return localStream.sequential();}

    @Override
    public Stream<T> parallel() {return localStream.parallel();}

    @Override
    public Stream<T> unordered() {return localStream.unordered();}

    @Override
    public Stream<T> onClose(Runnable closeHandler) {return localStream.onClose(closeHandler);}

    @Override
    public void close() {
        localStream.close();
    }

}
