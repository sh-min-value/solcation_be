package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long>{
    List<PushNotification> findTop2ByUserPk_UserPkOrderByPnTimeDesc(Long userPk);

    @Query(value = """
    SELECT COUNT(*)
    FROM push_notification_tb 
    WHERE group_pk = :groupPk AND ac_pk = 1 AND is_accepted = false
    """, nativeQuery = true)
    Long countPendingInvitationByGroupPk(@Param("groupPk") long groupPk);

    Optional<PushNotification> findByPnPkAndUserPk_UserPk(Long pnPk, Long userPk);
    List<PushNotification> findByUserPk_UserPkAndAcPkAndIsAcceptedOrderByPnTimeDesc(Long userPk, AlarmCategory acPk, Boolean isAccepted);

    @Query("""
    select new org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO(
      p.pnPk, p.pnTitle, p.pnTime, p.pnContent,
      ac.acDest, g.groupName, g.groupImage,
      p.isAccepted, p.readAt
    )
    from PushNotification p
    left join p.acPk ac
    left join p.groupPk g
    where p.userPk.userPk = :userPk
      and p.pnTime between :fromTime and :toTime
      and p.acPk.acPk <> :acPk
    order by p.pnTime desc
    """)
    Page<PushNotificationDTO> findRecent(@Param("userPk") Long userPk, @Param("fromTime") LocalDateTime fromTime, @Param("toTime") LocalDateTime toTime, @Param("acPk") Long acPk, Pageable pageable);

}
