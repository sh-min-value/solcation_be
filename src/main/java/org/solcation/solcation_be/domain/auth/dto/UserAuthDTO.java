package org.solcation.solcation_be.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.enums.ROLE;

@Schema(name = "로그인 시 유저 객체 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {
    private Long userPk;

    private String userId;

    private String userPw;

    private String tel;

    private String userName;

    private ROLE role;

    private String email;

}
