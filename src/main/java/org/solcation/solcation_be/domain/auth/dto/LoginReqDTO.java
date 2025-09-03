package org.solcation.solcation_be.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "로그인 응답 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginReqDTO {
    @NotBlank(message = "아이디를 입력해주세요")
    @NotNull
    @Schema(description = "아이디", defaultValue = "admin")
    private String userId;

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @NotNull
    @Schema(description = "비밀번호", defaultValue = "1234")
    private String userPw;
}
