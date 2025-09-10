package org.solcation.solcation_be.domain.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;

import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.util.redis.RedisKeys;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.TravelRepository;

@Service
@RequiredArgsConstructor
public class SnapshotCommitService {

    private static final int SCALE = 18; // DECIMAL(38,18) 스케일 고정

    private final RedissonClient redisson;
    private final ObjectMapper om;
    private final PlanDetailRepository planRepo;
    private final TravelRepository travelRepo;
    private final SimpMessagingTemplate messaging;

    // 단일 day 커밋
    @Transactional
    public void save(long travelId, int day, String clientId) {
        RLock lock = redisson.getLock(RedisKeys.saveLock(travelId, day));
        try {
            if (!lock.tryLock(5, 30, TimeUnit.SECONDS)) {
                throw new CustomException(ErrorCode.LOCKED);
            }

            var snapBucket = redisson.getBucket(RedisKeys.snapshot(travelId, day));
            Snapshot snap = readJson((String) snapBucket.get(), Snapshot.class);
            if (snap == null) snap = new Snapshot(new ArrayList<>(), "0-0");

            Travel travel = travelRepo.getReferenceById(travelId);

            Map<String, PlanDetail> byId = new HashMap<>();
            planRepo.findAliveByTravelAndDayByPdDayAscPositionAsc(travelId, day)
                    .forEach(e -> byId.put(e.getCrdtId(), e));

            for (PlanDetailDTO dto : snap.items()) {
                PlanDetail e = byId.remove(dto.getCrdtId());
                if (dto.isTombstone()) {
                    if (e != null) e.setTombstone(true);
                    continue;
                }
                if (e == null) e = new PlanDetail();
                e.setTravel(travel);
                e.setPdDay(dto.getPdDay());
                e.setPdPlace(dto.getPdPlace());
                e.setPdAddress(dto.getPdAddress());
                e.setPdCost(dto.getPdCost());
                e.setPosition(new BigDecimal(dto.getPosition()).setScale(SCALE, RoundingMode.UNNECESSARY));
                e.setCrdtId(dto.getCrdtId());
                e.setClientId(dto.getClientId());
                e.setOpTs(dto.getOpTs());
                e.setTombstone(false);
                planRepo.save(e);
            }

            for (PlanDetail rest : byId.values()) rest.setTombstone(true);

            // 커밋 성공 → dirtyDays에서 제거
            redisson.getSet(RedisKeys.dirtyDays(travelId)).remove(String.valueOf(day));

            // 알림
            messaging.convertAndSend("/topic/travel/"+travelId, Map.of("type","saved","day",day));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }

    // 트래블 단위로 dirty된 모든 day 커밋
    @Transactional
    public void saveDirty(long travelId, String clientId) {
        RSet<String> dirty = redisson.getSet(RedisKeys.dirtyDays(travelId));
        var days = new ArrayList<>(dirty.readAll());
        days.stream().map(Integer::parseInt).sorted().forEach(d -> save(travelId, d, clientId));
    }

    private record Snapshot(java.util.List<PlanDetailDTO> items, String lastStreamId) {}
    private <T> T readJson(String s, Class<T> c){ try { return s==null?null:om.readValue(s,c);} catch(Exception e){ throw new RuntimeException(e);} }
}
