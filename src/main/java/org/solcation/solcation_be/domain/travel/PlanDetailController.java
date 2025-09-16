package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.travel.service.EditSessionService;
import org.solcation.solcation_be.domain.travel.service.SnapshotCommitService;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlanDetailController {

    private final EditSessionService editSessionService;
    private final SnapshotCommitService snapshotCommitService;

    // 여행 편집 입장
    @MessageMapping("/group/{groupId}/travel/{travelId}/edit/join")
    public JoinPayload join(@DestinationVariable long travelId,
                              @Payload Map<String, Object> payload,
                              SimpMessageHeaderAccessor header) {
        String userId = (String) payload.get("userId");
        log.info("WS JOIN travelId={}, userId={}", travelId, userId);
        return editSessionService.join(travelId, userId);
    }

    // 여행 편집 퇴장
    @MessageMapping("/group/{groupId}/travel/{travelId}/edit/leave")
    public void leave(@DestinationVariable long travelId,
                        @Payload Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        log.info("WS LEAVE travelId={}, userId={}", travelId, userId);
        editSessionService.leave(travelId, userId);
    }

    // 여행 편집 저장
    @MessageMapping("/group/{groupId}/travel/{travelId}/edit/save")
    public void save(@DestinationVariable long travelId,
                       @Payload Map<String, Object> payload) {
        String clientId = (String) payload.get("clientId");
        log.info("WS SAVE travelId={}, clientId={}", travelId, clientId);
        editSessionService.leave(travelId, clientId);
        snapshotCommitService.saveDirty(travelId, clientId);
    }
}
