package org.solcation.solcation_be.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "encryption.aesgcm")
public record AesGcmProperties(
        @NotBlank String passphrase,
        @Min(1) int iterations,
        @Min(128) int keyLength,   // bits
        @Min(8) int saltLength,    // bytes
        @Min(12) int ivLength      // bytes
) {}