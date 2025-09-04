package org.solcation.solcation_be.alarm;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class AlarmTests {
    @Autowired
    private PushNotificationRepository pushNotificationRepository;

    @Test
    public void countPending() {
        Long result = pushNotificationRepository.countPendingInvitationByGroupPk(10);

        System.out.println(result);
    }
}
