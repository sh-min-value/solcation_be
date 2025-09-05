package org.solcation.solcation_be.domain.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseManager {
    private final ScheduledThreadPoolExecutor heartbeatPool;

    //userPk, emitters
    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitterList = new ConcurrentHashMap<>();

    //emitter, ScheduledFuture
    private final Map<SseEmitter, ScheduledFuture<?>> heartbeatTasks = new ConcurrentHashMap<>();


    /* emitter 생성 */
    public SseEmitter createEmitter(Long userPk) {
        SseEmitter emitter = new SseEmitter(0L);
        emitterList.computeIfAbsent(userPk, k -> new CopyOnWriteArrayList<>()).add(emitter);

        //15초 주기 하트비트
        ScheduledFuture<?> f = heartbeatPool.scheduleAtFixedRate(
                () -> sendHeartBeat(userPk, emitter), 0, 15, TimeUnit.SECONDS
        );

        heartbeatTasks.put(emitter, f);

        //실행 종료 시 실행 runnable
        Runnable r = () -> cleanUpEmitter(userPk, emitter);

        //연결 종료 시
        emitter.onCompletion(r);

        //타임아웃 시
        emitter.onTimeout(r);

        //에러 발생 시
        emitter.onError(e -> r.run());

        return emitter;
    }

    /* heartbeat 전송 */
    public void sendHeartBeat(Long userPk, SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("heartbeat").data("ok"));
        } catch(Exception e) {
            //전송 중 오류: emitter 삭제
            var task = heartbeatTasks.remove(emitter);

            //스레드 실행 취소
            if(task != null) {
                task.cancel(true);
            }

            //emitter 등록 취소
            emitterList.getOrDefault(userPk, new CopyOnWriteArrayList<>()).remove(emitter);

            //sse연결을 에러와 함께 즉시 취소
            try {
                emitter.completeWithError(e);
            } catch(Exception ignore) {}
        }
    }

    /* 메시지 전송 */
    public void sendNotificationToEmitter(Long userPk, PushNotification notification) {
        List<SseEmitter> emitters = emitterList.getOrDefault(userPk, new CopyOnWriteArrayList<>());

        if(!emitters.isEmpty()) {
            List<SseEmitter> deadEmitters = new ArrayList<>();
            for(SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("alarm")
                            .data(notification));
                } catch (Exception e) {
                    log.error("Error Sending SSE to user: {} with message: {}", userPk, notification);
                    deadEmitters.add(emitter);
                }
            }
            deadEmitters.forEach(i -> cleanUpEmitter(userPk, i));
        } else {
            log.warn("No emitter found for user: {}", userPk);
        }
    }

    /* clean up */
    public void cleanUpEmitter(Long userPk, SseEmitter emitter) {
        //hearbeatTask에서 삭제
        var task = heartbeatTasks.remove(emitter);

        //task 중지
        task.cancel(true);

        //emitterList에서 삭제
        emitterList.getOrDefault(userPk, new CopyOnWriteArrayList<>()).remove(emitter);
    }
}
