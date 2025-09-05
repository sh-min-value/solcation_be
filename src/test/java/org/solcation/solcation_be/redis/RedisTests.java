package org.solcation.solcation_be.redis;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.PushNotification;
import org.solcation.solcation_be.util.redis.RedisPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@SpringBootTest
public class RedisTests {
    @Autowired
    private RedisPublisher redisPublisher;

    @Test
    public void testSaveData() {
        PushNotification pn = PushNotification.builder()
                        .pnPk(1L)
                        .pnTitle("초대 수락")
                        .pnTime(LocalDateTime.now())
                        .pnContent("초대 수락?")
                        .acPk(AlarmCategory.builder().acPk(1L).acName("초대 수락").acDest("??").build())
                        .groupPk(Group.builder().groupPk(9L).build())
                        .isAccepted(false)
                        .readAt(LocalDateTime.now())
                        .build();

        redisPublisher.saveNotificationWithTTL(1L, pn);
    }

    @Test
    public void testRedisSubscriber() {
        redisPublisher.publish(1L, 1L);
    }
}
