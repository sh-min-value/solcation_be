package org.solcation.solcation_be.util.redis;

import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.PushNotification;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.KeyExpirationEventMessageListener;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;

@Slf4j
@Component
public class RedisExpirationListener extends KeyExpirationEventMessageListener {
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    public RedisExpirationListener(RedisMessageListenerContainer listenerContainer) { super(listenerContainer); }

    @Transactional
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();
        log.info("Redis key expired - key: {}", expiredKey);

        Long pnPk = Long.valueOf(expiredKey.split(":")[1]);
        if(expiredKey.startsWith("pn:")) {
            //group_member_tb에서 is_accepted false로 업데이트
            User user = pushNotificationRepository.findByPnPk(pnPk).getUserPk();
            GroupMember gm = groupMemberRepository.findByUser(user);

            gm.updateIsAccepted(false);

            groupMemberRepository.save(gm);
        }

        //push_notification_tb is_accepted true로 업데이트
        PushNotification pn = pushNotificationRepository.findByPnPk(pnPk);
        pn.updateIsAccepted(true, Instant.now());
        pushNotificationRepository.save(pn);
    }
}
