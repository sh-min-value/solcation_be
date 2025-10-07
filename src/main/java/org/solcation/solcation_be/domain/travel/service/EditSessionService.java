package org.solcation.solcation_be.domain.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.dto.Snapshot;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.util.redis.RedisKeys;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EditSessionService {

    private final RedissonClient redisson;
    private final ObjectMapper om;
    private final PlanDetailRepository pdRepo;
    private final SimpMessagingTemplate messaging;

    public JoinPayload join(long travelId, String userId, String sessionId) {
        RSet<String> members = redisson.getSet(RedisKeys.members(travelId), StringCodec.INSTANCE);
        members.add(userId);

        Iterable<String> keys = redisson.getKeys().getKeysByPattern("plan:snapshot:" + travelId + ":*");
        log.info("Found Redis snapshot keys: {}", keys);

        Map<Integer, Snapshot> plans = new HashMap<>();

        for (String key : keys) {
            String[] parts = key.split(":");
            if (parts.length >= 4) {
                try {
                    int day = Integer.parseInt(parts[3]);
                    RBucket<String> snapBucket = redisson.getBucket(key, StringCodec.INSTANCE);
                    String snapJson = snapBucket.get();

                    if (snapJson != null) {
                        Snapshot snap = readJson(snapJson, Snapshot.class);
                        plans.put(day, snap);
                        log.info("Loaded snapshot for day {}: {}", day, snap);
                    }
                } catch (Exception e) {
                    log.error("Error processing key {}: {}", key, e.getMessage());
                }
            }
        }

        if (plans.isEmpty()) {
            log.info("No Redis snapshots found, loading from DB");
            List<Integer> days = pdRepo.findTravelDays(travelId);
            log.info("Found days from DB: {}", days);

            for (Integer day : days) {
                try {
                    var planList = pdRepo.findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day)
                            .stream().map(PlanDetailDTO::entityToDTO).toList();

                    if (!planList.isEmpty()) {
                        Snapshot snapshot = new Snapshot(planList, "0-0");
                        plans.put(day, snapshot);
                        log.info("Created snapshot from DB for day {}: {}", day, snapshot);

                        // DB에서 가져온 데이터를 Redis에도 저장
                        String redisKey = RedisKeys.snapshot(travelId, day);
                        RBucket<String> snapBucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
                        snapBucket.set(writeJson(snapshot));
                    }
                } catch (Exception e) {
                    log.error("Error creating snapshot for day {}: {}", day, e.getMessage());
                }
            }
        }

        SimpMessageHeaderAccessor headers = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headers.setSessionId(sessionId);
        headers.setLeaveMutable(true);

        Map<String, Object> payload = Map.of(
                "type", "join-response",
                "message", "전체 snapshot 준비 완료",
                "snapshot", plans
        );


        log.info("Sending join-response to sessionId: {}", sessionId);
        log.info("Payload: {}", payload);
        log.info(plans.toString());

        // 세션(사용자)에게 직접 전송
        messaging.convertAndSendToUser(sessionId, "/topic/travel/" + travelId, payload, headers.getMessageHeaders());

        // 동시에 편의상 presence 브로드캐스트(다른 사용자에게는 입장 알림)
        messaging.convertAndSend("/topic/travel/" + travelId, Map.of("type","presence-join","userId",userId));

        log.info(plans.toString());
        return new JoinPayload("ok", "0-0");
    }

    public void leave(long travelId, String userId) {
        redisson.getSet(RedisKeys.members(travelId), StringCodec.INSTANCE).remove(userId);
        messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                "type","presence-leave","userId",userId
        ));
    }

    public void delete(Long travelId) {
        RSet<String> members = redisson.getSet("plan:members:" + travelId, StringCodec.INSTANCE);
        members.delete();

        RKeys keys = redisson.getKeys();

        // Snapshot 삭제
        Iterable<String> snapshotKeys = keys.getKeysByPattern("plan:snapshot:" + travelId + ":*");
        for (String key : snapshotKeys) {
            redisson.getBucket(key, StringCodec.INSTANCE).delete();
        }

        // Stream 삭제
        Iterable<String> streamKeys = keys.getKeysByPattern("plan:stream:" + travelId + ":*");
        for (String key : streamKeys) {
            redisson.getStream(key, StringCodec.INSTANCE).delete();
        }

        messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                "type","deleted"
        ));
    }


    private <T> T readJson(String s, Class<T> c){ try { return s == null ? null : om.readValue(s,c);} catch(Exception e){ throw new RuntimeException(e);} }
    private String writeJson(Object o){ try { return om.writeValueAsString(o);} catch(Exception e){ throw new RuntimeException(e);} }
}
