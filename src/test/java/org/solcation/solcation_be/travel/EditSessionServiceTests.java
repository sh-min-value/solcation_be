package org.solcation.solcation_be.travel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.*;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.solcation.solcation_be.config.AuditingTestConfig;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.service.EditSessionService;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.util.redis.RedisKeys;

@ActiveProfiles("test")
@SpringJUnitConfig
@ContextConfiguration(classes = EditSessionServiceTests.TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(AuditingTestConfig.class)
class EditSessionServiceTests {

    @TestConfiguration
    static class TestConfig {

        @Bean ObjectMapper objectMapper() { return new ObjectMapper(); }

        @Bean RedissonClient redissonClient(ObjectMapper om) {
            Config cfg = new Config();
            cfg.useSingleServer().setAddress("redis://localhost:6379");
            cfg.setCodec(new JsonJacksonCodec(om));
            return Redisson.create(cfg);
        }

        @Bean
        PlanDetailRepository planDetailRepository() { return mock(PlanDetailRepository.class); }
        @Bean SimpMessagingTemplate messaging() { return mock(SimpMessagingTemplate.class); }

        @Bean
        EditSessionService editSessionService(
                RedissonClient redisson, ObjectMapper om,
                PlanDetailRepository pdRepo, SimpMessagingTemplate messaging) {
            return new EditSessionService(redisson, om, pdRepo, messaging);
        }
    }

    @Autowired EditSessionService service;
    @Autowired RedissonClient redisson;
    @Autowired PlanDetailRepository pdRepo;
    @Autowired SimpMessagingTemplate messaging;
    @Autowired ObjectMapper om;

    @BeforeEach
    void clean() {
        redisson.getKeys().flushdb();
        reset(pdRepo, messaging);
    }

    @Test
    void join_shouldInitializeSnapshotFromDB_whenAbsent() {
        long travelId = 100L; int day = 1; String userId = "u1";

        // PlanDetail 엔티티 목 -> DTO로 변환될 필드들 스텁
        PlanDetail e = mock(PlanDetail.class);
        when(e.getPdPlace()).thenReturn("제주시");
        when(e.getPdAddress()).thenReturn("공항");
        when(e.getPdCost()).thenReturn(0);
        when(e.getPdDay()).thenReturn(day);
        when(e.getPosition()).thenReturn(BigDecimal.valueOf(1));
        when(e.getCrdtId()).thenReturn("crdt:client");
        when(e.getClientId()).thenReturn("client");
        when(e.getOpTs()).thenReturn(123L);
        when(e.isTombstone()).thenReturn(false);

        when(pdRepo.findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day))
                .thenReturn(List.of(e));

        JoinPayload payload = service.join(travelId, userId, "0");

        // presence set 확인
        assertTrue(redisson.getSet(RedisKeys.members(travelId)).contains(userId));

        String snapJson = (String) redisson.getBucket(RedisKeys.snapshot(travelId, day), StringCodec.INSTANCE).get();
        assertNotNull(snapJson);

        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messaging, atLeastOnce()).convertAndSend(eq("/topic/travel/" + travelId), payloadCap.capture());
        Object sent = payloadCap.getValue();
        assertTrue(sent instanceof Map);
        assertEquals("presence-join", ((Map<?, ?>) sent).get("type"));

        verify(pdRepo, times(1)).findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day);
        assertNotNull(payload);
    }

    @Test
    void join_shouldReuseExistingSnapshot_whenPresent() {
        long travelId = 101L; int day = 2; String userId = "u2";

        // 미리 스냅샷 삽입 (DB 조회 없이 써야 함)
        var dto = PlanDetailDTO.builder()
                .pdPk(null).pdPlace("미리있음").pdAddress("A").pdCost(0)
                .pdDay(day).position("1").crdtId("id1").clientId("c").opTs(1L)
                .tombstone(false).build();
        String preSnap = writeJson(Map.of("items", List.of(dto), "lastStreamId", "0-0"));
        redisson.getBucket(RedisKeys.snapshot(travelId, day)).set(preSnap);

        // 실행
        JoinPayload payload = service.join(travelId, userId,"0");

        // DB는 호출되지 않아야 함
        verify(pdRepo, never()).findAliveByTravelAndDayByPdDayAscPositionAsc(anyLong(), anyInt());

        // 스냅샷은 그대로 재사용
        assertEquals("미리있음", parse(payload.snapshotJson()).path("items").get(0).path("pdPlace").asText());
    }

    @Test
    void leave_shouldRemoveMemberAndBroadcast() {
        long travelId = 102L; int day = 1; String userId = "ux";
        redisson.getSet(RedisKeys.members(travelId)).add(userId);

        service.leave(travelId, userId);

        assertFalse(redisson.getSet(RedisKeys.members(travelId)).contains(userId));

        ArgumentCaptor<Object> payloadCap = ArgumentCaptor.forClass(Object.class);
        verify(messaging, atLeastOnce()).convertAndSend(eq("/topic/travel/" + travelId), payloadCap.capture());
        Object sent = payloadCap.getValue();
        assertInstanceOf(Map.class, sent);
        assertEquals("presence-leave", ((Map<?, ?>) sent).get("type"));
    }

    private JsonNode parse(String json) {
        try { return om.readTree(json); } catch (Exception e) { throw new AssertionError(e); }
    }
    private String writeJson(Object o) {
        try { return om.writeValueAsString(o); } catch (Exception e) { throw new RuntimeException(e); }
    }
}
