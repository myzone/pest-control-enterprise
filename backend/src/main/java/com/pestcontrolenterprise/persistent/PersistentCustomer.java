package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.Address;
import com.pestcontrolenterprise.api.AdminSession;
import com.pestcontrolenterprise.api.Customer;
import com.pestcontrolenterprise.api.InvalidStateException;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import static com.pestcontrolenterprise.api.InvalidStateException.inactiveSession;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentCustomer extends PersistentObject implements Customer {

    @Id
    protected final String name;

    @Type(type = "serializable")
    @Column(length = 8192)
    protected volatile Address address;

    @Column
    protected volatile String cellPhone;

    @Column
    protected volatile String email;

    @Deprecated
    protected PersistentCustomer() {
        super();

        name = null;
    }

    public PersistentCustomer(ApplicationContext applicationContext, String name, Address address, String cellPhone, String email) {
        super(applicationContext);

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
        try (QuiteAutoCloseable lock = readLock()) {
            return address;
        }
    }

    @Override
    public String getCellPhone() {
        try (QuiteAutoCloseable lock = readLock()) {
            return cellPhone;
        }
    }

    @Override
    public String getEmail() {
        try (QuiteAutoCloseable lock = readLock()) {
            return email;
        }
    }

    @Override
    public void setAddress(AdminSession session, Address address) throws InvalidStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive(getApplicationContext().getClock()))
                throw inactiveSession();

            this.address = address;
        }
    }

    @Override
    public void setCellPhone(AdminSession session, String cellPhone) throws InvalidStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive(getApplicationContext().getClock()))
                throw inactiveSession();

            this.cellPhone = cellPhone;
        }
    }

    @Override
    public void setEmail(AdminSession session, String email) throws InvalidStateException {
        try (QuiteAutoCloseable lock = writeLock()) {
            if (!session.isStillActive(getApplicationContext().getClock()))
                throw inactiveSession();

            this.email = email;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentCustomer)) return false;

        PersistentCustomer that = (PersistentCustomer) o;

        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        try (QuiteAutoCloseable lock = readLock()) {
            return Objects.toStringHelper(this)
                    .add("name", name)
                    .add("address", address)
                    .add("cellPhone", cellPhone)
                    .add("email", email)
                    .toString();
        }
    }

}
