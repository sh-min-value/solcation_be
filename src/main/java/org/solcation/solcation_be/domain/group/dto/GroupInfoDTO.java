package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(name = "그룹 메인 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
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
