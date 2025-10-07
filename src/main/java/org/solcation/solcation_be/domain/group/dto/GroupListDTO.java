package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.solcation.solcation_be.entity.GroupCategory;
import org.solcation.solcation_be.entity.User;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "그룹 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class GroupListDTO {
    //그룹 pk
    @NotNull
    private Long groupPk;

    //그룹 이름
    @NotNull
    private String groupName;

    //그룹 프로필
    @NotNull
    private String profileImg;

    //그룹 카테고리 코드
    @NotNull
    private String gcPk;

    //개설자 이름
    @NotNull
    private String groupLeader;

    //총 명수
    @NotNull
    private Integer totalMembers;

    //예정된 여정 개수
    private Long scheduled;
}
