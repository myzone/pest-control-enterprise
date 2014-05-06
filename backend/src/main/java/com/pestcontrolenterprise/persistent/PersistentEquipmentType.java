package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.pestcontrolenterprise.api.EquipmentType;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.UUID;

/**
 * @author myzone
 * @date 4/28/14
 */
@Immutable
@Entity
public class PersistentEquipmentType implements EquipmentType {

    @Id
    private final long id = UUID.randomUUID().getLeastSignificantBits();

    @Column
    protected final String name;

    public PersistentEquipmentType(String name) {
        this.name = name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentEquipmentType)) return false;

        PersistentEquipmentType that = (PersistentEquipmentType) o;

        if (id != that.id) return false;
        if (!name.equals(that.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .toString();
    }

}
