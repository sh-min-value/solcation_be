package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.User;

@Schema(name = "그룹 메인 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfoDTO {
    //그룹 pk
    @NotNull
    private Long groupPk;

    //그룹 이름
    @NotNull
    private String groupName;

    //그룹 이미지
    @NotNull
    private String profileImg;

    //그룹 카테고리 이름
    @NotNull
    private String gcPk;

    //개설자 이름
    @NotNull
    private String groupLeader;

    //총 멤버 수
    @NotNull
    private Integer totalMembers;

    //완료된 여정
    @NotNull
    private Long finished;

    //예정된 여정
    @NotNull
    private Long scheduled;

    //대기 중인 초대
    @NotNull
    private Long pending;
}
