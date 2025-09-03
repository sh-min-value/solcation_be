package org.solcation.solcation_be.entity.converter;

import jakarta.persistence.Converter;
import org.solcation.solcation_be.entity.TRAVELSTATE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

@Converter(autoApply=true)
public class TravelStateConverter extends AbstractLegacyEnumAttributeConverter<TRAVELSTATE> {

    protected TravelStateConverter() {
        super(TRAVELSTATE.class);
    }
}
