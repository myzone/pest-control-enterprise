package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.api.Address;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

/**
 * @author myzone
 * @date 4/28/14
 */
@Immutable
public class PersistentAddress implements Address, Serializable {

    protected final String representation;

    public PersistentAddress(String representation) {
        this.representation = representation;
    }

    @Override
    public String getRepresentation() {
        return representation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentAddress)) return false;

        PersistentAddress that = (PersistentAddress) o;

        if (!representation.equals(that.representation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return representation.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("representation", representation)
                .toString();
    }

}
