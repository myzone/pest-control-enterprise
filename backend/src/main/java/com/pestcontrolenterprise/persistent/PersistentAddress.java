package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.api.Address;

import javax.persistence.*;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentAddress implements Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    protected volatile String representation;

    public PersistentAddress() {
    }

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
        if (o == null || getClass() != o.getClass()) return false;

        PersistentAddress that = (PersistentAddress) o;

        if (id != that.id) return false;
        if (!representation.equals(that.representation)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + representation.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("representation", representation)
                .toString();
    }

}
