package org.solcation.solcation_be.domain.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.entity.PushNotification;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.solcation.solcation_be.util.redis.RedisPublisher;
import org.solcation.solcation_be.util.redis.RedisSubscriber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;


@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {
    private final SseManager sseManager;
    private final PushNotificationRepository notificationRepository;
    private final RedisPublisher redisPublisher;

    /* sse 연결 (emitter 생성) */
    public SseEmitter connectSse(Long userPk) {
        return sseManager.createEmitter(userPk);
    }

    /* 알림 DB 저장 및 publish */
    @Transactional
    public void saveNotification(Long userPk, PushNotification pushNotification) {
        //DB 저장
        notificationRepository.save(pushNotification);

        //트랜잭션 성공 시 notification redis publish
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redisPublisher.saveNotificationWithTTL(userPk, pushNotification);
                redisPublisher.publish(pushNotification.getPnPk(), userPk);
            }
        });
    }

    /* 알림 확인 여부 업데이트 */
    @Transactional
    public void updateCheck(Long pnPk, Long userPk) {
        PushNotification notification = (PushNotification) notificationRepository.findByPnPkAndUserPk_UserPk(pnPk, userPk).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        notification.updateIsAccepted(true, LocalDateTime.now());
        notificationRepository.save(notification);
    }
}
