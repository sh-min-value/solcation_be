package org.solcation.solcation_be.entity.converter;

import jakarta.persistence.Converter;
import org.solcation.solcation_be.entity.ROLE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

@Converter(autoApply=true)
public class UserRoleConverter extends AbstractLegacyEnumAttributeConverter<ROLE> {

    protected UserRoleConverter() {
        super(ROLE.class);
    }
}
