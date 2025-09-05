package org.solcation.solcation_be.util.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.notification.dto.PublishDTO;
import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Component
@Slf4j
public class RedisPublisher {
    private final RedisTemplate<String, Object> template;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.data.redis.notification.channel}")
    private String CHANNEL;

    private final Long TTL = 30L;

    /* 메시지를 특정 채널에 발행(pnPk, userPk) */
    public void publish(Long pnPk, Long userPk) {
        PublishDTO dto = PublishDTO.builder().pnPk(pnPk).userPk(userPk).build();
        log.info("Publishing message to channel: [{}] at time: {} with message: {}", CHANNEL, Instant.now(), dto);
        template.convertAndSend(CHANNEL, dto);
    }

    /* 알림 데이터를 Redis에 저장(pnPk, notification) */
    public void saveNotificationWithTTL(Long pnPk, PushNotification notification) {
        try {
            String key = String.valueOf(pnPk);
            redisTemplate.opsForValue().set(key, notification, TTL, TimeUnit.DAYS);
            log.info("Save message: [{}] at time: {} with notification pk: {}", CHANNEL, Instant.now(), notification.getPnPk());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}
