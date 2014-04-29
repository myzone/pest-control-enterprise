package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.api.EquipmentType;
import com.pestcontrolenterprise.api.PestType;

import javax.persistence.*;
import java.util.Set;

/**
 * @author myzone
 * @date 4/28/14
 */
@Entity
public class PersistentPestType implements PestType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column
    protected volatile String name;

    @Column
    protected volatile String describtion;

    @ManyToMany(targetEntity = PersistentEquipmentType.class)
    protected volatile Set<EquipmentType> requiredEquipmentTypes;

    public PersistentPestType() {
    }

    public PersistentPestType(String name, String describtion) {
        this.name = name;
        this.describtion = describtion;
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
    public String getDescription() {
        return describtion;
    }

    @Override
    public ImmutableSet<EquipmentType> getRequiredEquipmentTypes() {
        return ImmutableSet.copyOf(requiredEquipmentTypes);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentPestType)) return false;

        PersistentPestType that = (PersistentPestType) o;

        if (id != that.id) return false;
        if (!describtion.equals(that.describtion)) return false;
        if (!name.equals(that.name)) return false;
        if (!requiredEquipmentTypes.equals(that.requiredEquipmentTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + name.hashCode();
        result = 31 * result + describtion.hashCode();
        result = 31 * result + requiredEquipmentTypes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("describtion", describtion)
                .toString();
    }

}
