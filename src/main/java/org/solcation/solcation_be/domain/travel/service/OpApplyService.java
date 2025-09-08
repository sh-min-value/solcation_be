package org.solcation.solcation_be.domain.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.redisson.api.*;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamTrimArgs;
import org.redisson.client.codec.StringCodec;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.redis.RedisKeys;
import org.solcation.solcation_be.domain.travel.util.Positioning;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;

@Service
@RequiredArgsConstructor
public class OpApplyService {

    private final RedissonClient redisson;
    private final ObjectMapper om;
    private final SimpMessagingTemplate messaging;

    public void handleOp(long travelId, OpMessage op) {
        // === 멱등 ===
        RBucket<String> seen = redisson.getBucket(RedisKeys.op(op.opId()));
        if (!seen.trySet("1", 24, TimeUnit.HOURS)) {
            return; // 이미 처리한 op
        }

        switch (op.type()) {
            case "insert", "move", "update", "delete" -> applySingleDay(travelId, op, op.day());
            case "moveDay" -> {
                int oldDay = op.day();
                int newDay = ((Number) op.payload().get("newDay")).intValue();
                applyMoveDay(travelId, op, oldDay, newDay);
            }
            default -> throw new CustomException(ErrorCode.UNKNOWN_MOD_TYPE);
        }
    }

    // insert/move/update/delete : 한 날짜 스냅샷만 수정
    private void applySingleDay(long travelId, OpMessage op, int day) {
        RLock lock = redisson.getLock(RedisKeys.editLock(travelId, day));
        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.BUSY_RESOURCE);

            // 스냅샷 로드
            RBucket<String> snapBucket =
                    redisson.getBucket(RedisKeys.snapshot(travelId, day), StringCodec.INSTANCE);
            String snapJson = snapBucket.get();
            Snapshot snap = readSnap(snapJson);
            if (snap == null) snap = new Snapshot(new ArrayList<>(), "0-0");

            // 메모리 수정
            List<PlanDetailDTO> items = new ArrayList<>(snap.items());
            applyInMemory(items, op);

            // 스냅샷 저장
            snapBucket.set(writeJson(new Snapshot(items, snap.lastStreamId())));

            // 스트림 기록 + lastStreamId 갱신 + trim
            RStream<String, String> stream =
                    redisson.getStream(RedisKeys.stream(travelId, day), StringCodec.INSTANCE);

            StreamMessageId sid =
                    stream.add(StreamAddArgs.entries(toFields(op, day)));

            stream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));

            // 마지막 스트림 ID로 스냅샷 갱신
            snapBucket.set(writeJson(new Snapshot(items, sid.toString())));

            // dirty day 표시
            RSet<String> dirty = redisson.getSet(RedisKeys.dirtyDays(travelId), StringCodec.INSTANCE);
            dirty.add(String.valueOf(day));

            // 브로드캐스트
            messaging.convertAndSend("/topic/travel/" + travelId, Map.of(
                    "type","applied","streamId", sid.toString(), "day", day, "op", op
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (lock.isHeldByCurrentThread()) lock.unlock();
        }
    }


    // moveDay : 두 날짜 스냅샷을 모두 수정 (락 순서 보장)
    private void applyMoveDay(long travelId, OpMessage op, int oldDay, int newDay) {
        int d1 = Math.min(oldDay, newDay);
        int d2 = Math.max(oldDay, newDay);

        RLock l1 = redisson.getLock(RedisKeys.editLock(travelId, d1));
        RLock l2 = redisson.getLock(RedisKeys.editLock(travelId, d2));

        try {
            if (!l1.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.BUSY_RESOURCE);
            if (!l2.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.BUSY_RESOURCE);

            // 스냅샷 로드
            var srcBucket = redisson.getBucket(RedisKeys.snapshot(travelId, oldDay));
            var dstBucket = redisson.getBucket(RedisKeys.snapshot(travelId, newDay));
            Snapshot src = readSnap((String) srcBucket.get()); if (src == null) src = new Snapshot(new ArrayList<>(), "0-0");
            Snapshot dst = readSnap((String) dstBucket.get()); if (dst == null) dst = new Snapshot(new ArrayList<>(), "0-0");

            var itemsSrc = new ArrayList<>(src.items());
            var itemsDst = new ArrayList<>(dst.items());

            // payload에서 대상 항목
            String id = (String) op.payload().get("crdtId");
            String prev = norm((String) op.payload().get("prevCrdtId"));
            String next = norm((String) op.payload().get("nextCrdtId"));

            // 소스에서 제거
            PlanDetailDTO t = find(itemsSrc, id);
            itemsSrc.remove(t);

            // 타겟에 삽입 (position 계산)
            BigDecimal pos = computePos(itemsDst, newDay, prev, next);
            t.setPdDay(newDay);
            t.setPosition(pos.toPlainString());
            t.setClientId(op.clientId());
            t.setOpTs(op.opTs());
            itemsDst.add(t);

            // 소스/타겟 스냅샷 저장
            srcBucket.set(writeJson(new Snapshot(itemsSrc, src.lastStreamId())));
            dstBucket.set(writeJson(new Snapshot(itemsDst, dst.lastStreamId())));

            // 스트림 기록(양쪽) + lastStreamId 갱신 + trim
            RStream<String, String> srcStream =
                    redisson.getStream(RedisKeys.stream(travelId, oldDay), StringCodec.INSTANCE);
            RStream<String, String> dstStream =
                    redisson.getStream(RedisKeys.stream(travelId, newDay), StringCodec.INSTANCE);

            StreamMessageId sidOut =
                    srcStream.add(StreamAddArgs.entries(toMoveOutFields(op, oldDay)));
            srcStream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));
            srcBucket.set(writeJson(new Snapshot(itemsSrc, sidOut.toString())));

            StreamMessageId sidIn =
                    dstStream.add(StreamAddArgs.entries(toMoveInFields(op, newDay, t)));
            dstStream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));
            dstBucket.set(writeJson(new Snapshot(itemsDst, sidIn.toString())));

            // dirty days 표시
            var set = redisson.getSet(RedisKeys.dirtyDays(travelId));
            set.add(String.valueOf(oldDay));
            set.add(String.valueOf(newDay));

            // 브로드캐스트
            messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                    "type","applied","moveDay", Map.of("from", oldDay, "to", newDay), "op", op
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (l2.isHeldByCurrentThread()) l2.unlock();
            if (l1.isHeldByCurrentThread()) l1.unlock();
        }
    }

    // Inmemory 적용
    private void applyInMemory(List<PlanDetailDTO> list, OpMessage op) {
        Map<String,Object> p = op.payload();
        switch (op.type()) {
            case "insert" -> {
                String prev = norm((String)p.get("prevCrdtId"));
                String next = norm((String)p.get("nextCrdtId"));
                int day = (int) p.getOrDefault("pdDay", op.day());
                BigDecimal pos = computePos(list, day, prev, next);
                String crdtId = java.util.UUID.randomUUID() + ":" + op.clientId();

                PlanDetailDTO dto = PlanDetailDTO.builder()
                        .pdPk(null)
                        .pdDay(day)
                        .pdPlace((String)p.get("pdPlace"))
                        .pdAddress((String)p.get("pdAddress"))
                        .pdCost(((Number)p.getOrDefault("pdCost", 0)).intValue())
                        .position(pos.toPlainString())
                        .crdtId(crdtId)
                        .clientId(op.clientId())
                        .opTs(op.opTs())
                        .tombstone(false)
                        .build();
                list.add(dto);
            }
            case "move" -> {
                String id = (String)p.get("crdtId");
                String prev = norm((String)p.get("prevCrdtId"));
                String next = norm((String)p.get("nextCrdtId"));
                var t = find(list, id);
                BigDecimal pos = computePos(list, t.getPdDay(), prev, next);
                t.setPosition(pos.toPlainString());
                t.setClientId(op.clientId());
                t.setOpTs(op.opTs());
            }
            case "update" -> {
                String id = (String)p.get("crdtId");
                var t = find(list, id);
                if (p.get("pdPlace")   != null) t.setPdPlace((String)p.get("pdPlace"));
                if (p.get("pdAddress") != null) t.setPdAddress((String)p.get("pdAddress"));
                if (p.get("pdCost")    != null) t.setPdCost(((Number)p.get("pdCost")).intValue());
                t.setClientId(op.clientId());
                t.setOpTs(op.opTs());
            }
            case "delete" -> {
                String id = (String)p.get("crdtId");
                var t = find(list, id);
                t.setTombstone(true);
                t.setClientId(op.clientId());
                t.setOpTs(op.opTs());
            }
        }
        list.sort(Comparator
                .comparing(PlanDetailDTO::getPdDay)
                .thenComparing(a -> new BigDecimal(a.getPosition()))
                .thenComparing(PlanDetailDTO::getOpTs, Comparator.nullsLast(Long::compareTo))
                .thenComparing(PlanDetailDTO::getClientId, Comparator.nullsLast(String::compareTo))
                .thenComparing(PlanDetailDTO::getCrdtId, Comparator.nullsLast(String::compareTo)));
    }

    private Map<String, String> toFields(OpMessage op, int day) {
        return Map.of(
                "type", op.type(),
                "opId", op.opId(),
                "clientId", op.clientId(),
                "opTs", String.valueOf(op.opTs()),
                "day", String.valueOf(day),
                "payload", writeJson(op.payload())
        );
    }
    private Map<String, String> toMoveOutFields(OpMessage op, int oldDay) {
        return Map.of(
                "type", "moveOut",
                "opId", op.opId(),
                "clientId", op.clientId(),
                "opTs", String.valueOf(op.opTs()),
                "day", String.valueOf(oldDay),
                "payload", writeJson(Map.of("crdtId", op.payload().get("crdtId")))
        );
    }
    private Map<String, String> toMoveInFields(OpMessage op, int newDay, PlanDetailDTO dto) {
        return Map.of(
                "type", "moveIn",
                "opId", op.opId(),
                "clientId", op.clientId(),
                "opTs", String.valueOf(op.opTs()),
                "day", String.valueOf(newDay),
                "payload", writeJson(Map.of(
                        "crdtId", dto.getCrdtId(),
                        "pdDay", dto.getPdDay(),
                        "position", dto.getPosition()
                ))
        );
    }

    /* ===== 유틸 ===== */
    private PlanDetailDTO find(List<PlanDetailDTO> list, String crdtId) {
        return list.stream().filter(x -> crdtId.equals(x.getCrdtId()))
                .findFirst().orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    private BigDecimal computePos(List<PlanDetailDTO> all, int day, String prev, String next) {
        var alive = all.stream()
                .filter(x -> !x.isTombstone() && x.getPdDay() == day)
                .sorted(Comparator.comparing(a -> new BigDecimal(a.getPosition())))
                .toList();

        if (prev == null && next == null) {
            if (alive.isEmpty()) return new BigDecimal("1");
            var tail = alive.get(alive.size()-1);
            return Positioning.after(new BigDecimal(tail.getPosition()));
        }
        if (prev == null) {
            var n = alive.stream().filter(x -> x.getCrdtId().equals(next)).findFirst().orElseThrow();
            return Positioning.before(new BigDecimal(n.getPosition()));
        }
        if (next == null) {
            var p = alive.stream().filter(x -> x.getCrdtId().equals(prev)).findFirst().orElseThrow();
            return Positioning.after(new BigDecimal(p.getPosition()));
        }
        var p = alive.stream().filter(x -> x.getCrdtId().equals(prev)).findFirst().orElseThrow();
        var n = alive.stream().filter(x -> x.getCrdtId().equals(next)).findFirst().orElseThrow();
        var mid = Positioning.mid(new BigDecimal(p.getPosition()), new BigDecimal(n.getPosition()));
        if (mid.compareTo(new BigDecimal(p.getPosition())) <= 0 || mid.compareTo(new BigDecimal(n.getPosition())) >= 0) {
            BigDecimal step = new BigDecimal("10"), cur = step;
            for (var a : alive) { a.setPosition(cur.toPlainString()); cur = cur.add(step); }
            return computePos(all, day, prev, next);
        }
        return mid;
    }

    private String norm(String s) { if (s==null) return null; var t=s.trim(); return (t.isEmpty()||"0".equals(t)||"null".equalsIgnoreCase(t))?null:t; }

    /* 내부 VO + JSON */
    private record Snapshot(java.util.List<PlanDetailDTO> items, String lastStreamId) {}
    private Snapshot readSnap(String s){ try { return s==null?null:om.readValue(s, Snapshot.class);} catch(Exception e){throw new RuntimeException(e);} }
    private String writeJson(Object o){ try { return om.writeValueAsString(o);} catch(Exception e){throw new RuntimeException(e);} }
}
