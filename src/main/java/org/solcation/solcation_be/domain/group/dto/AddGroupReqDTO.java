package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.solcation.solcation_be.entity.enums.GROUPCODE;
import org.springframework.web.multipart.MultipartFile;

@Schema(name = "그룹 생성 요청 DTO")
@Getter
@Builder
@AllArgsConstructor
public class AddGroupReqDTO {
    //그룹 카테고리
    @NotNull
    private GROUPCODE gcPk;

    //그룹 이름
    @NotNull
    @NotBlank(message = "그룹 이름을 작성해주세요.")
    private String groupName;

    //그룹 프로필
    @NotNull
    private MultipartFile profileImg;
}
