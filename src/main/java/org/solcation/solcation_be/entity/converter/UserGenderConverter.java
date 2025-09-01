package org.solcation.solcation_be.entity.converter;

import org.solcation.solcation_be.entity.GENDER;
import org.solcation.solcation_be.entity.ROLE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

public class UserGenderConverter extends AbstractLegacyEnumAttributeConverter<GENDER> {
    protected UserGenderConverter() {
        super(GENDER.class);
    }
}
