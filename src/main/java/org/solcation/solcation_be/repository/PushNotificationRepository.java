package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.main.dto.NotificationPreviewDTO;
import org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.PushNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PushNotificationRepository extends JpaRepository<PushNotification, Long>{
    // 메인페이지 알림 미리보기
    @Query(value = """
    select ac.ac_code as acCode,
           g.group_name as groupName,
           gl.user_name as userName,
           p.pn_title as pnTitle
    from push_notification_tb p
    join alarm_category_tb ac on p.ac_pk = ac.ac_pk
    join group_tb g on p.group_pk = g.group_pk
    join user_tb gl on g.group_leader = gl.user_pk
    where p.user_pk = :userPk
      and p.is_accepted = false
    order by p.pn_time desc
    limit :limit
    """, nativeQuery = true)
    List<NotificationPreviewDTO> findPreviewByUser(@Param("userPk") Long userPk, @Param("limit") int limit);

    @Query(value = """
    SELECT COUNT(*)
    FROM push_notification_tb 
    WHERE group_pk = :groupPk AND ac_pk = 1 AND is_accepted = false
    """, nativeQuery = true)
    Long countPendingInvitationByGroupPk(@Param("groupPk") long groupPk);

    Optional<PushNotification> findByPnPkAndUserPk_UserPk(Long pnPk, Long userPk);

    //유저가 읽지 않은 그룹 초대 목록 조회
    List<PushNotification> findByUserPk_UserPkAndAcPkAndIsAcceptedOrderByPnTimeDesc(Long userPk, AlarmCategory acPk, Boolean isAccepted);

    //최근 N일 알림 목록 렌더링 (읽음 무관)
    @Query("""
    select new org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO(
      p.pnPk, p.pnTitle, p.pnTime, p.pnContent,
      ac.acDest, g.groupPk, g.groupName, g.groupImage,
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
    Page<PushNotificationDTO> findRecent(@Param("userPk") Long userPk, @Param("fromTime") Instant fromTime, @Param("toTime") Instant toTime, @Param("acPk") Long acPk, Pageable pageable);

    PushNotification findByPnPk(@Param("pnPk") Long pnPk);
}
