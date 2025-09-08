package org.solcation.solcation_be.entity.converter;

import jakarta.persistence.Converter;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

@Converter(autoApply=true)
public class DepositDayConverter extends AbstractLegacyEnumAttributeConverter<DEPOSITDAY> {

    protected DepositDayConverter() {
        super(DEPOSITDAY.class);
    }
}
