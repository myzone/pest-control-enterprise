package com.pestcontrolenterprise.util;

import com.google.common.base.Objects;

import java.io.Serializable;

/**
 * @author myzone
 * @date 28-Apr-14
 */
public class ImmutableSegment<T> implements Segment<T>, Serializable {

    protected final T start;
    protected final T end;

    public ImmutableSegment(T start, T end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public T getStart() {
        return start;
    }

    @Override
    public T getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImmutableSegment that = (ImmutableSegment) o;

        if (!end.equals(that.end)) return false;
        if (!start.equals(that.start)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start.hashCode();
        result = 31 * result + end.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("start", start)
                .add("end", end)
                .toString();
    }

}
