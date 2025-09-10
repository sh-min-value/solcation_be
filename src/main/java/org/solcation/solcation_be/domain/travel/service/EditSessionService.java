package org.solcation.solcation_be.domain.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.redisson.api.RBucket;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.util.redis.RedisKeys;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;
import org.solcation.solcation_be.repository.PlanDetailRepository;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EditSessionService {
    private final RedissonClient redisson;
    private final ObjectMapper om;
    private final PlanDetailRepository pdRepo;
    private final SimpMessagingTemplate messaging;

    public JoinPayload join(long travelId, int day, String userId) {
        // 프레즌스 추가
        RSet<String> members = redisson.getSet(RedisKeys.members(travelId, day));
        members.add(userId);

        // 스냅샷 초기화(없으면 DB에서)
        RBucket<String> snapBucket = redisson.getBucket(RedisKeys.snapshot(travelId, day));
        String snapJson = snapBucket.get();
        if (snapJson == null) {
            var list = pdRepo.findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day)
                    .stream().map(PlanDetailDTO::entityToDTO).toList();
            snapJson = writeJson(new Snapshot(list, "0-0"));
            snapBucket.set(snapJson);
        }

        // 입장 브로드캐스트
        messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                "type","presence-join","day",day,"userId",userId
        ));

        Snapshot snap = readJson(snapJson, Snapshot.class);
        return new JoinPayload(snapJson, snap.lastStreamId());
    }

    public void leave(long travelId, int day, String userId) {
        redisson.getSet(RedisKeys.members(travelId, day)).remove(userId);
        messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                "type","presence-leave","day",day,"userId",userId
        ));
    }

    public record Snapshot(java.util.List<PlanDetailDTO> items, String lastStreamId) {}
    private <T> T readJson(String s, Class<T> c){ try { return om.readValue(s,c);} catch(Exception e){ throw new RuntimeException(e);} }
    private String writeJson(Object o){ try { return om.writeValueAsString(o);} catch(Exception e){ throw new RuntimeException(e);} }
}
