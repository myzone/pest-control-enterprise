package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.api.Address;
import com.pestcontrolenterprise.api.Consumer;

import javax.persistence.*;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentConsumer implements Consumer {

    @Id
    protected volatile String name;

    @Embedded
    @Column
    protected volatile Address address;

    @Column
    protected volatile String cellPhone;

    @Column
    protected volatile String email;

    public PersistentConsumer() {
    }

    public PersistentConsumer(String name, Address address, String cellPhone, String email) {
        this.name = name;
        this.address = address;
        this.cellPhone = cellPhone;
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Address getAddress() {
        return address;
    }

    @Override
    public String getCellPhone() {
        return cellPhone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PersistentConsumer that = (PersistentConsumer) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("address", address)
                .add("cellPhone", cellPhone)
                .add("email", email)
                .toString();
    }

}
