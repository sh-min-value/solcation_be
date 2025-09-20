package org.solcation.solcation_be.domain.travel.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.redisson.api.*;
import org.redisson.api.stream.StreamAddArgs;
import org.redisson.api.stream.StreamTrimArgs;
import org.redisson.client.codec.StringCodec;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.dto.Snapshot;
import org.solcation.solcation_be.domain.travel.util.Positioning;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;
import org.solcation.solcation_be.util.redis.RedisKeys;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OpApplyService {

    private final RedissonClient redisson;
    private final ObjectMapper om;
    private final SimpMessagingTemplate messaging;

    public void handleOp(long travelId, OpMessage op) {
        RBucket<String> seen = redisson.getBucket(RedisKeys.op(op.opId()));
        if (!seen.trySet("1", 24, TimeUnit.HOURS)) return;

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

    private void applySingleDay(long travelId, OpMessage op, int day) {
        RLock lock = redisson.getLock(RedisKeys.editLock(travelId, day));
        try {
            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.LOCKED);

            RBucket<String> snapBucket = redisson.getBucket(RedisKeys.snapshot(travelId, day), StringCodec.INSTANCE);
            Snapshot snap = readSnap(snapBucket.get());
            if (snap == null) snap = new Snapshot(new ArrayList<>(), "0-0");

            List<PlanDetailDTO> items = new ArrayList<>(snap.items());
            applyInMemory(items, op);

            snapBucket.set(writeJson(new Snapshot(items, snap.lastStreamId())));

            RStream<String, String> stream = redisson.getStream(RedisKeys.stream(travelId, day), StringCodec.INSTANCE);
            StreamMessageId sid = stream.add(StreamAddArgs.entries(toFields(op, day)));
            stream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));

            snapBucket.set(writeJson(new Snapshot(items, sid.toString())));

            RSet<String> dirty = redisson.getSet(RedisKeys.dirtyDays(travelId), StringCodec.INSTANCE);
            dirty.add(String.valueOf(day));

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

    private void applyMoveDay(long travelId, OpMessage op, int oldDay, int newDay) {
        int d1 = Math.min(oldDay, newDay);
        int d2 = Math.max(oldDay, newDay);

        RLock l1 = redisson.getLock(RedisKeys.editLock(travelId, d1));
        RLock l2 = redisson.getLock(RedisKeys.editLock(travelId, d2));

        try {
            if (!l1.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.LOCKED);
            if (!l2.tryLock(1, 5, TimeUnit.SECONDS)) throw new CustomException(ErrorCode.LOCKED);

            var srcBucket = redisson.getBucket(RedisKeys.snapshot(travelId, oldDay), StringCodec.INSTANCE);
            var dstBucket = redisson.getBucket(RedisKeys.snapshot(travelId, newDay), StringCodec.INSTANCE);
            Snapshot src = readSnap((String) srcBucket.get()); if (src == null) src = new Snapshot(new ArrayList<>(), "0-0");
            Snapshot dst = readSnap((String) dstBucket.get()); if (dst == null) dst = new Snapshot(new ArrayList<>(), "0-0");

            List<PlanDetailDTO> itemsSrc = new ArrayList<>(src.items());
            List<PlanDetailDTO> itemsDst = new ArrayList<>(dst.items());

            String id = (String) op.payload().get("crdtId");
            String prev = Positioning.normalize((String) op.payload().get("prevCrdtId"));
            String next = Positioning.normalize((String) op.payload().get("nextCrdtId"));

            PlanDetailDTO t = find(itemsSrc, id);
            t.setTombstone(true);
            t.setClientId(op.clientId());
            t.setOpTs(op.opTs());

            String newCrdtId = UUID.randomUUID() + ":" + op.clientId();
            BigDecimal pos = Positioning.computePos(itemsDst, newDay, prev, next);

            PlanDetailDTO newItem = PlanDetailDTO.builder()
                    .pdDay(newDay)
                    .pdPlace(t.getPdPlace())
                    .pdAddress(t.getPdAddress())
                    .tcCode(t.getTcCode())
                    .pdCost(t.getPdCost())
                    .position(pos.toPlainString())
                    .crdtId(newCrdtId)
                    .clientId(op.clientId())
                    .opTs(op.opTs())
                    .tombstone(false)
                    .build();

            itemsDst.add(newItem);

            srcBucket.set(writeJson(new Snapshot(itemsSrc, src.lastStreamId())));
            dstBucket.set(writeJson(new Snapshot(itemsDst, dst.lastStreamId())));

            RStream<String, String> srcStream = redisson.getStream(RedisKeys.stream(travelId, oldDay), StringCodec.INSTANCE);
            RStream<String, String> dstStream = redisson.getStream(RedisKeys.stream(travelId, newDay), StringCodec.INSTANCE);

            StreamMessageId sidOut = srcStream.add(StreamAddArgs.entries(toMoveOutFields(op, oldDay)));
            srcStream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));
            srcBucket.set(writeJson(new Snapshot(itemsSrc, sidOut.toString())));

            StreamMessageId sidIn = dstStream.add(StreamAddArgs.entries(toMoveInFields(op, newDay, newItem)));
            dstStream.trim((StreamTrimArgs) StreamTrimArgs.maxLen(2000));
            dstBucket.set(writeJson(new Snapshot(itemsDst, sidIn.toString())));

            RSet<String> dirty = redisson.getSet(RedisKeys.dirtyDays(travelId), StringCodec.INSTANCE);
            dirty.add(String.valueOf(oldDay));
            dirty.add(String.valueOf(newDay));

            messaging.convertAndSend("/topic/travel/"+travelId, Map.of(
                    "type","applied","moveDay", Map.of("from", oldDay, "to", newDay),
                    "op", op, "newCrdtId", newCrdtId  // 클라이언트에 새 CRDT ID 전달
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (l2.isHeldByCurrentThread()) l2.unlock();
            if (l1.isHeldByCurrentThread()) l1.unlock();
        }
    }

    private void applyInMemory(List<PlanDetailDTO> list, OpMessage op) {
        Map<String,Object> p = op.payload();
        switch (op.type()) {
            case "insert" -> {
                String prev = Positioning.normalize((String)p.get("prevCrdtId"));
                String next = Positioning.normalize((String)p.get("nextCrdtId"));
                int day = (int) p.getOrDefault("pdDay", op.day());
                BigDecimal pos = Positioning.computePos(list, day, prev, next);
                String crdtId = UUID.randomUUID() + ":" + op.clientId();

                PlanDetailDTO dto = PlanDetailDTO.builder()
                        .pdDay(day)
                        .pdPlace((String)p.get("pdPlace"))
                        .pdAddress((String)p.get("pdAddress"))
                        .tcCode((String)p.get("tcCode"))
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
                String prev = Positioning.normalize((String)p.get("prevCrdtId"));
                String next = Positioning.normalize((String)p.get("nextCrdtId"));
                var t = find(list, id);
                BigDecimal pos = Positioning.computePos(list, t.getPdDay(), prev, next);
                t.setPosition(pos.toPlainString());
                t.setClientId(op.clientId());
                t.setOpTs(op.opTs());
            }
            case "update" -> {
                String id = (String)p.get("crdtId");
                var t = find(list, id);
                if (p.get("pdCost") != null) t.setPdCost(((Number)p.get("pdCost")).intValue());
                if(p.get("tcCode") != null) t.setTcCode((String)p.get("tcCode"));
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
                "tcCode", op.tcCode(),
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
                "tcCode", String.valueOf(op.tcCode()),
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
                "tcCode", dto.getTcCode(),
                "payload", writeJson(Map.of(
                        "crdtId", dto.getCrdtId(),
                        "pdDay", dto.getPdDay(),
                        "position", dto.getPosition()
                ))
        );
    }

    private PlanDetailDTO find(List<PlanDetailDTO> list, String crdtId) {
        return list.stream().filter(x -> crdtId.equals(x.getCrdtId()))
                .findFirst().orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
    }

    private Snapshot readSnap(String s){ try { return s == null ? null : om.readValue(s, Snapshot.class); } catch(Exception e){ throw new RuntimeException(e); } }
    private String writeJson(Object o){ try { return om.writeValueAsString(o); } catch(Exception e){ throw new RuntimeException(e); } }
}
