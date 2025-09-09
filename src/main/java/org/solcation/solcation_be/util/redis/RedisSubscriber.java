package org.solcation.solcation_be.util.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.notification.SseManager;
import org.solcation.solcation_be.domain.notification.dto.PublishDTO;
import org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO;
import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Slf4j
@RequiredArgsConstructor
@Component
public class RedisSubscriber {
    private final RedisTemplate<String, PushNotificationDTO> redisTemplate;
    private final ScheduledExecutorService exService = Executors.newScheduledThreadPool(10);
    private final SseManager sseManager;

    public void onMessage(String channel, PublishDTO message) {
        log.info("Received message from channel: [{}] at time: {} with message: {}", channel, Instant.now(), message.getPnPk());

        //redis에서 키 조회 + sse전송
        processMessage(message.getPnPk(), message.getUserPk(), 5);

    }

    public void processMessage(Long pnPk, Long userPk, int attempt) {
        exService.submit(() -> {
           try {
               PushNotificationDTO pn = null;
               for(int i = 0; i < attempt; i++) {
                   pn = (PushNotificationDTO) redisTemplate.opsForValue().get(String.valueOf(pnPk));

                   if(pn != null) {
                       break;
                   }
                   try {
                       Thread.sleep(200);
                   } catch (InterruptedException e) {
                       Thread.currentThread().interrupt();
                       return;
                   }
               }

               if(pn != null) {
                   sseManager.sendNotificationToEmitter(userPk, pn);
               } else {
                   log.warn("No notification found in Redis for key: {}", pnPk);
               }
           } catch (Exception e) {
               log.error("Exception while processing push notification", e);
           }
        });
    }
}
