package org.solcation.solcation_be.util.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;

public abstract class AbstractLegacyEnumAttributeConverter <E extends Enum<E> & LegacyCommonType> implements AttributeConverter<E, Integer> {

    private final Class<E> enumType;

    protected AbstractLegacyEnumAttributeConverter(Class<E> enumType) {
        this.enumType = enumType;
    }

    /* enum -> column */
    @Override
    public Integer convertToDatabaseColumn(E e) {
        return (e == null) ? null : e.getCode();
    }

    /* colum -> enum */
    @Override
    public E convertToEntityAttribute(Integer integer) {
        return (integer == null) ? null: LegacyEnumUtils.of(enumType, integer);
    }
}
