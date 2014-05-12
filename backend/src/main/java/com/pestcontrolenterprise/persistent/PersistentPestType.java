package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.EquipmentType;
import com.pestcontrolenterprise.api.PestType;
import org.hibernate.annotations.Immutable;

import javax.annotation.Generated;
import javax.persistence.*;
import java.util.Set;
import java.util.UUID;

/**
 * @author myzone
 * @date 4/28/14
 */
@Immutable
@Entity
public class PersistentPestType extends PersistentObject implements PestType {

    @Id
    protected final String name;

    @Column
    protected final String description;

    @ManyToMany(targetEntity = PersistentEquipmentType.class)
    protected volatile Set<EquipmentType> requiredEquipmentTypes;

    @Deprecated
    protected PersistentPestType() {
        super();

        name = null;
        description = null;
    }

    public PersistentPestType(ApplicationContext applicationContext, String name, String description, Set<EquipmentType> requiredEquipmentTypes) {
        super(applicationContext);

        this.name = name;
        this.description = description;
        this.requiredEquipmentTypes = requiredEquipmentTypes;

        save();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
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

        if (!description.equals(that.description)) return false;
        if (!name.equals(that.name)) return false;
        if (!requiredEquipmentTypes.equals(that.requiredEquipmentTypes)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + name.hashCode();
        result = 31 * result + description.hashCode();
        result = 31 * result + requiredEquipmentTypes.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("description", description)
                .toString();
    }

}
