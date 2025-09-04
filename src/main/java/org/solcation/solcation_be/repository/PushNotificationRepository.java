package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long> {
    List<PushNotification> findTop2ByUserPk_UserPkOrderByPnTimeDesc(Long userPk);

    @Query(value = """
    SELECT COUNT(*)
    FROM push_notification_tb 
    WHERE group_pk = :groupPk AND ac_pk = 1 AND is_accepted = false
    """, nativeQuery = true)
    Long countPendingInvitationByGroupPk(@Param("groupPk") long groupPk);
}
