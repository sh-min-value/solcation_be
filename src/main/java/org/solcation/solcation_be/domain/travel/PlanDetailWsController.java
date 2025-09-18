package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.travel.service.EditSessionService;
import org.solcation.solcation_be.domain.travel.service.OpApplyService;
import org.solcation.solcation_be.domain.travel.service.SnapshotCommitService;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlanDetailWsController{
    private final OpApplyService opApplyService;
    private final EditSessionService editSessionService;
    private final SnapshotCommitService snapshotCommitService;

    // 여행 편집 입장
    @MessageMapping("/group/{groupId}/travel/{travelId}/edit/join")
    public JoinPayload join(@DestinationVariable long travelId,
                            @Payload Map<String, Object> payload,
                            SimpMessageHeaderAccessor header) {
        String userId = (String) payload.get("userId");
        log.info("WS JOIN travelId={}, userId={}", travelId, userId);
        return editSessionService.join(travelId, userId, header.getSessionId());
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

    @MessageMapping("/group/{groupId}/travel/{travelId}/edit/op")
    public void onOp(@DestinationVariable long groupId,
                     @DestinationVariable long travelId,
                     @Payload Map<String, Object> body) {
        try {
            OpMessage msg = OpMessage.from(body); // Map -> OpMessage
            log.info("WS OP recv travelId={}, type={}, opId={}", travelId, msg.type(), msg.opId());
            opApplyService.handleOp(travelId, msg);
        } catch (Exception e) {
            log.error("WS onOp error: {}", e.getMessage(), e); // 예외 삼키기: 세션 끊김 방지
        }
    }
}
