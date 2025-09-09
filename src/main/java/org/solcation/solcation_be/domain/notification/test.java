package org.solcation.solcation_be.domain.notification;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class test {
    @Value("${spring.data.redis.notification.channel}")
    private String CHANNEL;

    @PostConstruct
    void logChannel() { log.info("REDIS NOTIF CHANNEL = [{}]", CHANNEL); }
}
