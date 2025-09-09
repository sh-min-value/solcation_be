package org.solcation.solcation_be.travel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.redisson.Redisson;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.solcation.solcation_be.domain.travel.service.OpApplyService;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringJUnitConfig
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
            cfg.useSingleServer().setAddress("redis://localhost:6379");
            // OpApplyService 내부는 JSON으로 직렬화/역직렬화하도록
            cfg.setCodec(new JsonJacksonCodec(objectMapper));
            return Redisson.create(cfg);
        }

        @Bean
        OpApplyService opApplyService(RedissonClient redisson, ObjectMapper objectMapper) {
            return new OpApplyService(redisson, objectMapper, Mockito.mock(SimpMessagingTemplate.class));
        }
    }

    @Autowired OpApplyService service;
    @Autowired RedissonClient redisson;
    @Autowired ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        redisson.getKeys().flushdb();
    }

    private String snapshotKey(long travelId, int day) {
        return "plan:snapshot:" + travelId + ":" + day;
    }

    private JsonNode readSnapshotJson(long travelId, int day) {
        RBucket<String> bucket = redisson.getBucket(snapshotKey(travelId, day), StringCodec.INSTANCE);
        String json = bucket.get();
        assertNotNull(json, "snapshot json should not be null for day=" + day);
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new AssertionError("Failed to parse snapshot json: " + e.getMessage() + "\njson=" + json, e);
        }
    }

    private List<JsonNode> readItems(long travelId, int day) {
        JsonNode root = readSnapshotJson(travelId, day);
        JsonNode items = root.path("items");
        assertTrue(items.isArray(), "snapshot.items must be array");
        List<JsonNode> list = new ArrayList<>();
        items.forEach(list::add);
        return list;
    }
    private List<JsonNode> readAliveItems(long travelId, int day) {
        return readItems(travelId, day).stream()
                .filter(n -> !n.path("tombstone").asBoolean(false))
                .toList();
    }

    private List<String> readAliveItemIds(long travelId, int day) {
        return readAliveItems(travelId, day).stream()
                .map(n -> n.path("crdtId").asText(null))
                .filter(Objects::nonNull)
                .toList();
    }

    private Optional<JsonNode> findItemById(long travelId, int day, String crdtId) {
        return readItems(travelId, day).stream()
                .filter(n -> crdtId.equals(n.path("crdtId").asText(null)))
                .findFirst();
    }

    private String insertOpId() {
        return UUID.randomUUID().toString();
    }

    private void insertItem(long travelId, int day, String place, String address, int cost, String prev, String next) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("pdDay", day);
        payload.put("prevCrdtId", prev);
        payload.put("nextCrdtId", next);
        payload.put("pdPlace", place);
        payload.put("pdAddress", address);
        payload.put("pdCost", cost);

        var op = new OpMessage(
                "insert",
                insertOpId(),
                "test-user",
                System.currentTimeMillis(),
                day,
                payload
        );
        service.handleOp(travelId, op);
    }

    private void moveItem(long travelId, int fromDay, String crdtId, String newPrev, String newNext) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("crdtId", crdtId);
        payload.put("prevCrdtId", newPrev);
        payload.put("nextCrdtId", newNext);

        var op = new OpMessage(
                "move",
                UUID.randomUUID().toString(),
                "test-user",
                System.currentTimeMillis(),
                fromDay,
                payload
        );
        service.handleOp(travelId, op);
    }

    private void updateItem(long travelId, int day, String crdtId, Map<String, Object> fieldsToUpdate) {
        Map<String, Object> payload = new HashMap<>(fieldsToUpdate);
        payload.put("crdtId", crdtId);

        var op = new OpMessage(
                "update",
                UUID.randomUUID().toString(),
                "test-user",
                System.currentTimeMillis(),
                day,
                payload
        );
        service.handleOp(travelId, op);
    }

    private void deleteItem(long travelId, int day, String crdtId) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("crdtId", crdtId);

        var op = new OpMessage(
                "delete",
                UUID.randomUUID().toString(),
                "test-user",
                System.currentTimeMillis(),
                day,
                payload
        );
        service.handleOp(travelId, op);
    }

    private void moveDay(long travelId, int srcDay, int dstDay, String crdtId, String prevId, String nextId) {
        var op = new OpMessage(
                "moveDay",
                UUID.randomUUID().toString(),
                "test-user",
                System.currentTimeMillis(),
                /* day = */ srcDay,                        // ← oldDay는 여기
                Map.of(
                        "newDay", dstDay,                 // ← newDay는 payload
                        "crdtId", crdtId,
                        "prevCrdtId", prevId,
                        "nextCrdtId", nextId
                )
        );
        service.handleOp(travelId, op);
    }


    @Test
    void insert_shouldAppearInSnapshot() {
        long travelId = 10L;
        int day = 1;

        insertItem(travelId, day, "제주시", "공항", 0, null, null);
        insertItem(travelId, day, "제주시", "공항", 0, null, null);
        String json = (String) redisson.getBucket(snapshotKey(travelId, day), StringCodec.INSTANCE).get();
        assertNotNull(json);
        assertTrue(json.contains("\"items\""));
        assertTrue(json.contains("\"제주시\""));
    }

    @Test
    void move_shouldReorderWithinDay() {
        long travelId = 11L;
        int day = 1;

        // A, B 순서로 삽입 (맨 끝에 추가)
        insertItem(travelId, day, "A", "addrA", 100, null, null);
        insertItem(travelId, day, "B", "addrB", 200, null, null);

        // 현재 순서 확인
        List<String> ids = readAliveItemIds(travelId, day);
        assertEquals(2, ids.size());
        String A = ids.get(0);
        String B = ids.get(1);

        // B를 A앞으로 이동시키기 => prev=null, next=A
        moveItem(travelId, day, B, null, A);

        // 순서가 B, A가 되었는지 확인
        List<String> after = readAliveItemIds(travelId, day);
        assertEquals(List.of(B, A), after);
    }

    @Test
    void update_shouldChangeFields() {
        long travelId = 12L;
        int day = 1;

        insertItem(travelId, day, "원래장소", "원래주소", 0, null, null);

        String id = readAliveItemIds(travelId, day).get(0);

        // 장소/주소/비용 수정
        Map<String, Object> patch = new HashMap<>();
        patch.put("pdPlace", "수정장소");
        patch.put("pdAddress", "수정주소");
        patch.put("pdCost", 777);

        updateItem(travelId, day, id, patch);

        JsonNode item = findItemById(travelId, day, id).orElseThrow();
        assertEquals("수정장소", item.path("pdPlace").asText());
        assertEquals("수정주소", item.path("pdAddress").asText());
        assertEquals(777, item.path("pdCost").asInt());
    }

    @Test
    void delete_shouldRemoveItem() {
        long travelId = 13L;
        int day = 1;

        insertItem(travelId, day, "삭제대상", "주소", 0, null, null);

        String id = readAliveItemIds(travelId, day).get(0);
        deleteItem(travelId, day, id);

        // 해당 id가 사라졌는지 확인
        List<String> ids = readAliveItemIds(travelId, day);
        assertFalse(ids.contains(id));
    }

    @Test
    void moveDay_shouldMoveItemToAnotherDay() {
        long travelId = 14L;
        int day1 = 1;
        int day2 = 2;

        insertItem(travelId, day1, "이동대상", "D1", 0, null, null);
        String id = readAliveItemIds(travelId, day1).get(0);

        // day1 -> day2 의 맨 앞에 이동 (prev=null, next=null이면 맨 끝으로 처리될 수도 있음)
        moveDay(travelId, day1, day2, id, "0", "0");

        // day1에는 없어야 하고
        List<String> idsDay1 = readAliveItemIds(travelId, day1);
        assertFalse(idsDay1.contains(id));

        // day2에 있어야 함
        List<String> idsDay2 = readAliveItemIds(travelId, day2);
        assertTrue(idsDay2.contains(id));
    }
}
