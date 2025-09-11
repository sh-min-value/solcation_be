package org.solcation.solcation_be.domain.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.notification.dto.PushNotificationDTO;
import org.solcation.solcation_be.domain.notification.dto.UpdateGroupInviteReqDTO;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.enums.ALARMCODE;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.PushNotification;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.solcation.solcation_be.util.category.AlarmCategoryLookup;
import org.solcation.solcation_be.util.redis.RedisPublisher;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationService {
    private final SseManager sseManager;
    private final RedisPublisher redisPublisher;
    private final PushNotificationRepository notificationRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final AlarmCategoryLookup alarmCategoryLookup;

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
                redisPublisher.saveNotificationWithTTL(pushNotification.getPnPk(), pushNotification);
                redisPublisher.publish(pushNotification.getPnPk(), userPk);
            }
        });
    }

    /* 알림 확인 여부 업데이트 */
    @Transactional
    public void updateCheck(Long pnPk, Long userPk) {
        PushNotification notification = (PushNotification) notificationRepository.findByPnPkAndUserPk_UserPk(pnPk, userPk).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        notification.updateIsAccepted(true, Instant.now());
        notificationRepository.save(notification);
    }

    /* 그룹 초대 목록 렌더링 */
    @Transactional
    public List<PushNotificationDTO> getInvitationList(Long userPk) {
        AlarmCategory ac = alarmCategoryLookup.get(ALARMCODE.GROUP_INVITE);
        List<PushNotification> result = pushNotificationRepository.findByUserPk_UserPkAndAcPkAndIsAcceptedOrderByPnTimeDesc(userPk, ac, false);
        List<PushNotificationDTO> list = new ArrayList<>();

        result.forEach(i -> list.add(PushNotificationDTO.builder()
                .pnPk(i.getPnPk())
                .title(i.getPnTitle())
                .pnTime(i.getPnTime())
                .acDest(i.getAcPk().getAcDest())
                .content(i.getPnContent())
                .groupName(i.getGroupPk().getGroupName())
                .groupImage(i.getGroupPk().getGroupImage())
                .isAccepted(i.getIsAccepted())
                .readAt(i.getReadAt())
                .build()
        ));

        return list;
    }

    /* 최근 7일 알림 목록 렌더링 */
    @Transactional
    public Page<PushNotificationDTO> getRecent7daysList(Long userPk, int pageNo, int pageSize) {
        //현재 시간 기준으로 7일 전까지 / 그룹 초대 제외
        AlarmCategory ac = alarmCategoryLookup.get(ALARMCODE.GROUP_INVITE);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        ZonedTimeRange r = ZonedTimeUtil.week(ZonedTimeUtil.now());

        Instant from = r.start();
        log.info("from: {}", from.toString());

        Instant to = r.end();
        log.info("to: {}", to.toString());

        return pushNotificationRepository.findRecent(userPk, from, to, ac.getAcPk(), pageable);

    }

    /* 최근 30일(8일 ~ 30일) 알림 목록 렌더링 */
    @Transactional
    public Page<PushNotificationDTO> getRecent30daysList(Long userPk, int pageNo, int pageSize) {
        //현재 시간 기준으로 8일 전 ~ 30일 전까지 / 그룹 초대 제외
        AlarmCategory ac = alarmCategoryLookup.get(ALARMCODE.GROUP_INVITE);
        Pageable pageable = PageRequest.of(pageNo, pageSize);

        LocalDate now = ZonedTimeUtil.now();
        ZonedTimeRange r = ZonedTimeUtil.custom(now.minusDays(30), now.minusDays(8));

        Instant from = r.start();
        log.info("from: {}", from.toString());

        Instant to = r.end();
        log.info("to: {}", to.toString());

        return pushNotificationRepository.findRecent(userPk, from, to, ac.getAcPk(), pageable);
    }

    /* 그룹 초대 수락/거절 */
    @Transactional
    public void updateGroupInvite(UpdateGroupInviteReqDTO dto, Long userPk) {
        //알림 읽음으로 업데이트
        PushNotification pn = notificationRepository.findByPnPk(dto.getPnPk());
        pn.updateIsAccepted(true, Instant.now());
        notificationRepository.save(pn);

        //그룹 멤버 is_accepted 업데이트
        GroupMember gm = groupMemberRepository.findByGroup_GroupPkAndUser_UserPkAndIsAcceptedIsNull(dto.getGroupPk(), userPk).orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        gm.updateIsAccepted(dto.getDecision());
        groupMemberRepository.save(gm);
    }
}
