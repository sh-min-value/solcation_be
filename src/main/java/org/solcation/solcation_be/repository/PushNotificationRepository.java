package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findTop2ByUserPk_UserPkOrderByPnTimeDesc(Long userPk);
}
