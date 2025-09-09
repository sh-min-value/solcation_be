package org.solcation.solcation_be.domain.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.AddGroupReqDTO;
import org.solcation.solcation_be.domain.group.dto.GroupInfoDTO;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.domain.group.dto.GroupMembersDTO;
import org.solcation.solcation_be.domain.notification.NotificationService;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.ALARMCODE;
import org.solcation.solcation_be.repository.*;
import org.solcation.solcation_be.util.category.AlarmCategoryLookup;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PushNotificationRepository pushNotificationRepository;
    private final UserRepository userRepository;
    private final S3Utils s3Utils;
    private final NotificationService notificationService;
    private final AlarmCategoryLookup alarmCategoryLookup;

    @Value("${cloud.s3.bucket.upload.profile.group}")
    private String UPLOAD_PATH;

    /* 그룹 생성 */
    @Transactional
    public boolean addGroup(AddGroupReqDTO addGroupReqDTO, User user) {
        GroupCategory gc = groupCategoryRepository.findByGcPk(addGroupReqDTO.getGcPk());

        //확장자 확인(png, jpeg, jpg)
        String originalFilename = addGroupReqDTO.getProfileImg().getOriginalFilename();

        if(!s3Utils.checkExtension(originalFilename)){
            throw new CustomException(ErrorCode.UNSUPPORTED_MEDIA_TYPE);
        }

        //이미지 업로드
        String filename = s3Utils.uploadObject(addGroupReqDTO.getProfileImg(), originalFilename, UPLOAD_PATH);

        //DB실패 시 이미지 삭제
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if(status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    try { s3Utils.deleteObject(filename, UPLOAD_PATH); } catch (Exception ignore) {}
                }
                TransactionSynchronization.super.afterCompletion(status);
            }
        });

        //그룹 생성
        Group group = Group.builder()
                .groupName(addGroupReqDTO.getGroupName())
                .groupImage(filename)
                .totalMembers(1)
                .gcPk(gc)
                .isCreated(false)
                .groupLeader(user)
                .build();

        groupRepository.save(group);

        //그룹 멤버 저장(group leader)
        GroupMember gm = GroupMember.builder()
                .group(group)
                .user(user)
                .role(true)
                .isAccepted(true)
                .isOut(false)
                .hasCard(false)
                .build();
        groupMemberRepository.save(gm);

        return true;
    }

    /* 그룹 목록 */
    public List<GroupListDTO> getList(String userId, String searchTerm) {
        List<GroupListDTO> result = new ArrayList<>();

        List<Object[]> results = groupRepository.getGroupListWithSearch(userId, searchTerm);

        for(Object[] obj: results) {
            GroupListDTO dto = GroupListDTO.builder()
                    .groupPk((Long) obj[0])
                    .groupName((String) obj[1])
                    .profileImg((String) obj[2])
                    .gcPk((GroupCategory) obj[3])
                    .groupLeader((User) obj[4])
                    .totalMembers((int) obj[5])
                    .scheduled((Long) obj[6])
                    .build();
            result.add(dto);
        }

        return result;
    }

    /* 그룹 메인 - 그룹 정보 렌더링 */
    public GroupInfoDTO getGroupInfo(Long groupPk) {
        //그룹 정보 조회
        Object[] result = (Object[])groupRepository.getGroupInfoByGroupPk(groupPk);

        //대기 중인 초대 수
        Long cnt = pushNotificationRepository.countPendingInvitationByGroupPk(groupPk);

        GroupInfoDTO dto = GroupInfoDTO.builder()
                .groupPk((Long) result[0])
                .groupName((String) result[1])
                .profileImg((String) result[2])
                .gcPk((GroupCategory) result[3])
                .groupLeader((User) result[4])
                .totalMembers((int) result[5])
                .finished((Long) result[6])
                .scheduled((Long) result[7])
                .pending(cnt)
                .build();

        return dto;
    }

    /* 그룹 메인 - 참여자 목록 */
    public GroupMembersDTO getGroupMembers(Long groupPk) {
        //그룹 개설자 조회
        User groupLeader = groupRepository.findGroupLeaderByGroupPk(groupPk);

        //그룹 멤버 조회
        List<User> groupMembers = groupMemberRepository
                .findByGroup_GroupPkAndRoleAndIsAcceptedOrderByUser_UserPkAsc(groupPk, false, true);

        //그룹 대기자 조회
        List<User> waitingMembers = groupMemberRepository
                .findByGroup_GroupPkAndRoleAndIsAcceptedOrderByUser_UserPkAsc(groupPk, false, false);


        return GroupMembersDTO.builder()
                .groupLeader(groupLeader)
                .members(groupMembers)
                .waitingList(waitingMembers)
                .build();
    }

    /* 그룹 메인 - 초대 전송 */
    public void inviteMembers(Long groupId, String tel) {
        //전화번호로 회원 조회
        User invitee = (User) userRepository.findByTel(tel).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //그룹 조회
        Group group = groupRepository.findByGroupPk(groupId);

        //초대 전송(알림 전송, 알림 저장)
        AlarmCategory ac = alarmCategoryLookup.get(ALARMCODE.GROUP_INVITE);

        PushNotification pn = PushNotification.builder()
                .pnTitle(ALARMCODE.GROUP_INVITE.getTitle())
                .pnTime(LocalDateTime.now())
                .pnContent(ALARMCODE.GROUP_INVITE.getContent())
                .acPk(ac)
                .userPk(invitee)
                .groupPk(group)
                .isAccepted(false)
                .build();

        notificationService.saveNotification(invitee.getUserPk(), pn);
    }
}
