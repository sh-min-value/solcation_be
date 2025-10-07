package org.solcation.solcation_be.util.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.solcation.solcation_be.security.AesGcmEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter(autoApply = false)
public class AesGcmAttributeConverter implements AttributeConverter<String, String> {
    private final AesGcmEncryptor encryptor;

    @Autowired
    public AesGcmAttributeConverter(AesGcmEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    /**
     * 암호화 (엔티티 필드 -> DB 칼럼 저장시)
     * @param attribute
     * @return
     */
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null || attribute.isEmpty()) return attribute;
        return encryptor.encrypt(attribute);
    }

    /**
     * 복호화 (DB 칼럼 -> 엔티티 필드 조회시)
     * @param dbData
     * @return
     */
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return dbData;
        return encryptor.decrypt(dbData);
    }
}
