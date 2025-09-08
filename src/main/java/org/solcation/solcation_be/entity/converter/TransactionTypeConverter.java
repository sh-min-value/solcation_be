package org.solcation.solcation_be.entity.converter;

import jakarta.persistence.Converter;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.util.entity.AbstractLegacyEnumAttributeConverter;

@Converter(autoApply=true)
public class TransactionTypeConverter extends AbstractLegacyEnumAttributeConverter<TRANSACTIONTYPE> {

    protected TransactionTypeConverter() {
        super(TRANSACTIONTYPE.class);
    }
}
