package org.solcation.solcation_be.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@TestConfiguration
public class AuditingTestConfig {
    @Bean
    @org.springframework.context.annotation.Primary
    public AuditorAware<Long> auditorAware() {
        // 모든 테스트에서 999L로 생성/수정자 주입
        return () -> Optional.of(999L);
    }
}
