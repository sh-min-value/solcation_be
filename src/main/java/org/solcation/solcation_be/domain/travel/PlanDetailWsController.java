package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.travel.service.OpApplyService;
import org.solcation.solcation_be.domain.travel.ws.OpMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PlanDetailWsController{
    private final OpApplyService opApplyService;

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
