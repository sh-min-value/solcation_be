package org.solcation.solcation_be.entity.converter;

import jakarta.persistence.Converter;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

@Converter(autoApply=true)
public class DepositCycleConverter extends AbstractLegacyEnumAttributeConverter<DEPOSITCYCLE> {

    protected DepositCycleConverter() {
        super(DEPOSITCYCLE.class);
    }
}
