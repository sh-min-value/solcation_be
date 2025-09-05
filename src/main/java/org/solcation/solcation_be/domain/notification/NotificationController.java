package org.solcation.solcation_be.domain.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.entity.PushNotification;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.solcation.solcation_be.util.redis.RedisPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "알림 컨트롤러")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {
    private final NotificationService notificationService;
    private final RedisPublisher redisPublisher;

    //테스트용
    @Operation(summary = "sse 연결", description = "emitter 생성")
    @GetMapping(value = "/conn", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connectSse() {
        return notificationService.connectSse(1L);
    }

//    @Operation(summary = "sse 연결", description = "emitter 생성")
//    @GetMapping(value = "/conn", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
//    public SseEmitter connectSse(@AuthenticationPrincipal JwtPrincipal  jwtPrincipal) {
//        return notificationService.connectSse(jwtPrincipal.userPk());
//    }

    @Operation(summary = "알림 생성 후 Redis 저장 + pubish 테스트")
    @PostMapping("/test")
    public void createAndPublish() {
        PushNotification pushNotification = PushNotification.builder().pnPk(1L).build();

        redisPublisher.saveNotificationWithTTL(1L, pushNotification);
        redisPublisher.publish(pushNotification.getPnPk(), 1L);
    }

    @Operation(summary = "알림 확인 여부 업데이트")
    @PostMapping("/check")
    public void check(@PathParam("pnPk") Long pnPk, @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
        notificationService.updateCheck(pnPk, jwtPrincipal.userPk());
    }
}
