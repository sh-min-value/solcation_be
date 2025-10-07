package org.solcation.solcation_be.config;

import org.solcation.solcation_be.security.AesGcmEncryptor;
import org.solcation.solcation_be.security.AesGcmProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AesGcmProperties.class)
public class CryptoConfig {
    @Bean
    public AesGcmEncryptor aesGcmEncryptor(AesGcmProperties props) {
        return new AesGcmEncryptor(props);
    }
}
