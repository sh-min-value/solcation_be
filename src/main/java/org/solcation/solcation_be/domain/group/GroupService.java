package org.solcation.solcation_be.domain.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.AddGroupReqDTO;
import org.solcation.solcation_be.domain.group.dto.GroupInfoDTO;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.GroupCategoryRepository;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.repository.PushNotificationRepository;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private final S3Utils s3Utils;

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
                    .profileImg(s3Utils.getPublicUrl((String) obj[2], UPLOAD_PATH))
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
                .profileImg(s3Utils.getPublicUrl((String) result[2], UPLOAD_PATH))
                .gcPk((GroupCategory) result[3])
                .groupLeader((User) result[4])
                .totalMembers((int) result[5])
                .finished((Long) result[6])
                .scheduled((Long) result[7])
                .pending(cnt)
                .build();

        return dto;
    }
}
