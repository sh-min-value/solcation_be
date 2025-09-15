package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.enums.GENDER;

import java.time.LocalDate;

@Schema(name = "그룹 멤버 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberFlatDTO {
    @NotNull
    private Long userPk;

    @NotNull
    private String userId;

    @NotNull
    private String tel;

    @NotNull
    private String userName;

    @NotNull
    private LocalDate dateOfBirth;

    @NotNull
    private GENDER gender;

    @NotNull
    private String email;

    @NotNull
    private Boolean isAccepted;

    @NotNull
    private Boolean role;
}
