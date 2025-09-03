package org.solcation.solcation_be.domain.group;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.AddGroupReqDTO;
import org.solcation.solcation_be.entity.Group;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.GroupCategoryRepository;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.GroupRepository;
import org.solcation.solcation_be.util.s3.S3Utils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Arrays;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupCategoryRepository groupCategoryRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final S3Utils s3Utils;

    @Value("${cloud.s3.bucket.upload.profile.group}")
    private String UPLOAD_PATH;

    @Transactional
    public boolean addGroup(AddGroupReqDTO addGroupReqDTO, User user) {
        GroupCategory gc = groupCategoryRepository.findByGcPk(addGroupReqDTO.getGcPk());

        //확장자 확인(png, jpeg, jpg)
        String originalFilename = addGroupReqDTO.getProfileImg().getOriginalFilename();

        if(!checkExtension(originalFilename)){
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

    /* 확장자 확인 확인 및 제한 */
    public boolean checkExtension(String filename) {
        if(filename.isEmpty()) {
            return false;
        }
        List<String> possibleExt = Arrays.asList(".jpg", ".jpeg", ".png");
        String extension = filename.substring(filename.lastIndexOf("."));

        return possibleExt.contains(extension);
    }
}
