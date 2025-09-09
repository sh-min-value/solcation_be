package org.solcation.solcation_be.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.solcation.solcation_be.domain.travel.service.OpApplyService;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringJUnitConfig // = @ExtendWith(SpringExtension.class) + @ContextConfiguration
@ContextConfiguration(classes = OpApplyServiceTests.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpApplyServiceTests {

    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        RedissonClient redissonClient(ObjectMapper objectMapper) {
            Config cfg = new Config();
            cfg.useSingleServer().setAddress("redis://localhost:6379")
                    .setPassword("1234");
            cfg.setCodec(new JsonJacksonCodec(objectMapper));
            return Redisson.create(cfg);
        }

        @Bean
        OpApplyService opApplyService(RedissonClient redisson, ObjectMapper objectMapper) {
            return new OpApplyService(redisson, objectMapper, Mockito.mock(SimpMessagingTemplate.class));
        }
    }

    @Autowired
    OpApplyService service;
    @Autowired
    RedissonClient redisson;

    @BeforeEach
    void setUp() {
        redisson.getKeys().flushdb();
    }

    @Test
    void insert_shouldAppearInSnapshot() {
        long travelId = 10L;
        int day = 1;

        var op = new OpMessage(
                "insert",
                UUID.randomUUID().toString(),
                "test-user",
                System.currentTimeMillis(),
                day,
                Map.of(
                        "pdDay", day,
                        "prevCrdtId", "0",
                        "nextCrdtId", "0",
                        "pdPlace", "제주시",
                        "pdAddress", "공항",
                        "pdCost", 0
                )
        );

        service.handleOp(travelId, op);

        String snapKey = "plan:snapshot:" + travelId + ":" + day;
        String json = (String) redisson.getBucket(snapKey, org.redisson.client.codec.StringCodec.INSTANCE).get();
        assertNotNull(json);
        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("\"제주시\""));
    }
}
