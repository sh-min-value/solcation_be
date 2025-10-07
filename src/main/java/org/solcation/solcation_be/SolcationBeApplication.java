package org.solcation.solcation_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ConfigurationPropertiesScan
@EnableJpaAuditing
@SpringBootApplication
public class SolcationBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SolcationBeApplication.class, args);
    }

}
