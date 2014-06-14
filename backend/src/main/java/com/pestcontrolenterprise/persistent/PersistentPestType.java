package com.pestcontrolenterprise.persistent;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.pestcontrolenterprise.ApplicationContext;
import com.pestcontrolenterprise.api.EquipmentType;
import com.pestcontrolenterprise.api.PestType;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Map;

import static java.util.Collections.emptyMap;

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

    @ElementCollection(targetClass = Integer.class)
    @MapKeyClass(PersistentEquipmentType.class)
    protected final Map<EquipmentType, Integer> requiredEquipment;

    @Deprecated
    protected PersistentPestType() {
        super();

        name = null;
        description = null;
        requiredEquipment = emptyMap();
    }

    public PersistentPestType(ApplicationContext applicationContext, String name, String description, Map<EquipmentType, Integer> requiredEquipmentTypes) {
        super(applicationContext);

        this.name = name;
        this.description = description;
        this.requiredEquipment = requiredEquipmentTypes;

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
    public ImmutableMap<EquipmentType, Integer> getRequiredEquipment() {
        return ImmutableMap.copyOf(requiredEquipment);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersistentPestType)) return false;

        PersistentPestType that = (PersistentPestType) o;

        if (!description.equals(that.description)) return false;
        if (!name.equals(that.name)) return false;
        if (!requiredEquipment.equals(that.requiredEquipment)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(
                name,
                description,
                requiredEquipment
        );
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("name", name)
                .add("description", description)
                .toString();
    }

}
