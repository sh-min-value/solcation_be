package org.solcation.solcation_be.domain.group;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.*;
import org.solcation.solcation_be.domain.notification.NotificationService;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.ALARMCODE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.repository.*;
import org.solcation.solcation_be.util.category.AlarmCategoryLookup;
import org.solcation.solcation_be.util.category.GroupCategoryLookup;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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
    private final GroupCategoryLookup groupCategoryLookup;

    @Value("${cloud.s3.bucket.upload.profile.group}")
    private String UPLOAD_PATH;

    /* 그룹 생성 */
    @Transactional
    public boolean addGroup(AddGroupReqDTO addGroupReqDTO, User user) {
        GroupCategory gc = groupCategoryLookup.get(addGroupReqDTO.getGcPk());

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
    @Transactional(readOnly = true)
    public List<GroupListDTO> getList(String userId, String searchTerm) {
        List<GroupListDTO> results = groupRepository.getGroupListWithSearch(userId, searchTerm, TRAVELSTATE.BEFORE);
        return results;
    }

    /* 그룹 메인 - 그룹 정보 렌더링 */
    @Transactional(readOnly = true)
    public GroupInfoDTO getGroupInfo(Long groupPk) {
        //그룹 정보 조회
        GroupInfoDTO result = groupRepository.getGroupInfoByGroupPk(groupPk, TRAVELSTATE.BEFORE, TRAVELSTATE.FINISH);
        return result;
    }

    /* 그룹 메인 - 참여자 목록 */
    @Transactional(readOnly = true)
    public GroupMembersDTO getGroupMembers(Long groupPk) {
        //그룹 개설자 조회
        User groupLeader = groupRepository.findGroupLeaderByGroupPk(groupPk);
        GroupMemberDTO leader = GroupMemberDTO.builder()
                .userPk(groupLeader.getUserPk())
                .userId(groupLeader.getUserId())
                .tel(groupLeader.getTel())
                .userName(groupLeader.getUserName())
                .dateOfBirth(groupLeader.getDateOfBirth())
                .gender(groupLeader.getGender())
                .email(groupLeader.getEmail())
                .build();
        //그룹 멤버 조회
        List<User> groupMembers = groupMemberRepository
                .findByGroup_GroupPkAndRoleAndIsAcceptedOrderByUser_UserPkAsc(groupPk, false, true);

        List<GroupMemberDTO> dtos = groupMembers.stream()
                .map(u -> new GroupMemberDTO(
                        u.getUserPk(),
                        u.getUserId(),
                        u.getTel(),
                        u.getUserName(),
                        u.getDateOfBirth(),
                        u.getGender(),
                        u.getEmail()
                ))
                .toList();

        //그룹 대기자 조회
        List<User> waitingMembers = groupMemberRepository
                .findByGroup_GroupPkAndPending(groupPk);

        List<GroupMemberDTO> dtos2 = waitingMembers.stream()
                .map(u -> new GroupMemberDTO(
                        u.getUserPk(),
                        u.getUserId(),
                        u.getTel(),
                        u.getUserName(),
                        u.getDateOfBirth(),
                        u.getGender(),
                        u.getEmail()
                ))
                .toList();

        return GroupMembersDTO.builder()
                .groupLeader(leader)
                .members(dtos)
                .waitingList(dtos2)
                .build();
    }

    /* 그룹 메인 - 초대 전송 */
    public void inviteMembers(Long groupId, String tel) {
        //전화번호로 회원 조회
        User invitee = (User) userRepository.findByTel(tel).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //그룹 조회
        Group group = groupRepository.findByGroupPk(groupId);

        //멤버 - isAccepted(true) | 거절 - isAccepted(false) | 대기 - isAccepted(null): 멤버/대기중인 회원 리스트에 존재 시 에러
        List<User> groupMemebers = groupMemberRepository.findByGroup_GroupPkAndNotRejected(group.getGroupPk());

        boolean exists = groupMemebers.stream().anyMatch(gm -> Objects.equals(invitee.getUserPk(), gm.getUserPk()));

        if(exists) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        //초대 전송(알림 전송, 알림 저장)
        AlarmCategory ac = alarmCategoryLookup.get(ALARMCODE.GROUP_INVITE);

        PushNotification pn = PushNotification.builder()
                .pnTitle(ALARMCODE.GROUP_INVITE.getTitle())
                .pnTime(Instant.now())
                .pnContent(ALARMCODE.GROUP_INVITE.getContent())
                .acPk(ac)
                .userPk(invitee)
                .groupPk(group)
                .isAccepted(false)
                .build();

        notificationService.saveNotification(invitee.getUserPk(), pn);

        //대기 중인 그룹 멤버 추가
        GroupMember groupMember = GroupMember.invitee(group, invitee);
        groupMemberRepository.save(groupMember);
    }

    /* 초대자 정보 조회 */
    public GroupMemberDTO getInvitee(String tel) {
        User user = userRepository.findByTel(tel).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return GroupMemberDTO.builder()
                .userPk(user.getUserPk())
                .userId(user.getUserId())
                .tel(user.getTel())
                .userName(user.getUserName())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .email(user.getEmail())
                .build();
    }
}
