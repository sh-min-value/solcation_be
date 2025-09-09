package org.solcation.solcation_be.travel;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.service.OpApplyService;
import org.solcation.solcation_be.domain.travel.service.SnapshotCommitService;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ActiveProfiles("test")
@SpringJUnitConfig // = @ExtendWith(SpringExtension.class) + @ContextConfiguration
@ContextConfiguration(classes = SnapshotCommitServiceTests.TestConfig.class)
class SnapshotCommitServiceTests {
    @TestConfiguration
    static class TestConfig {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        RedissonClient redissonClient(ObjectMapper objectMapper) {
            Config cfg = new Config();
            cfg.useSingleServer().setAddress("redis://localhost:6379");
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
    void save_shouldUpsertIntoRepository() {
        long travelId = 10L;
        int day = 1;

        // 1) Redis 스냅샷 미리 심기
        var dto = PlanDetailDTO.builder()
                .pdPk(null).pdPlace("제주시").pdAddress("공항").pdCost(0)
                .pdDay(day).position("1").crdtId("id:client").clientId("u1").opTs(1L)
                .tombstone(false).build();

        var snapJson = writeJson(Map.of("items", List.of(dto), "lastStreamId", "0-0"));
        redisson.getBucket("plan:snapshot:"+travelId+":"+day).set(snapJson);

        // 2) 레포, 메시징 mock
        PlanDetailRepository planRepo = mock(PlanDetailRepository.class);
        TravelRepository travelRepo = mock(TravelRepository.class);
        SimpMessagingTemplate messaging = mock(SimpMessagingTemplate.class);

        when(travelRepo.getReferenceById(travelId)).thenReturn(new Travel());
        when(planRepo.findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day))
                .thenReturn(List.of());

        SnapshotCommitService svc = new SnapshotCommitService(
                redisson, new ObjectMapper(), planRepo, travelRepo, messaging);

        // 3) 저장 호출
        svc.save(travelId, day, "u1");

        // 4) 저장된 엔티티 검증
        ArgumentCaptor<PlanDetail> cap = ArgumentCaptor.forClass(PlanDetail.class);
        verify(planRepo, atLeastOnce()).save(cap.capture());

        PlanDetail saved = cap.getValue();
        assertEquals("제주시", saved.getPdPlace());
        assertEquals("id:client", saved.getCrdtId());

        // dirtyDays에서 제거 확인(없음이 정상)
        var dirtySet = redisson.getSet("plan:dirtyDays:"+travelId);
        assertFalse(dirtySet.contains(String.valueOf(day)));
    }

    private static String writeJson(Object o) {
        try { return new ObjectMapper().writeValueAsString(o); }
        catch(Exception e){ throw new RuntimeException(e); }
    }
}
