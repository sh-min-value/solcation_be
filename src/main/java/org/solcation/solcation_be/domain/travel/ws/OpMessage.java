package org.solcation.solcation_be.domain.travel.ws;

import java.util.Map;

public record OpMessage(
        String type,      // insert | move | moveDay | update | delete
        String opId,      // 멱등 키(UUID)
        String clientId,
        long opTs,
        int day,
        String tcCode,
        Map<String,Object> payload
) {
    @SuppressWarnings("unchecked")
    public static OpMessage from(Map<String,Object> m) {
        return new OpMessage(
                String.valueOf(m.get("type")),
                String.valueOf(m.get("opId")),
                String.valueOf(m.get("clientId")),
                ((Number)m.get("opTs")).longValue(),
                ((Number)m.get("day")).intValue(),
                String.valueOf(m.get("tcCode")),
                (Map<String,Object>) m.getOrDefault("payload", Map.of())
        );
    }
}

