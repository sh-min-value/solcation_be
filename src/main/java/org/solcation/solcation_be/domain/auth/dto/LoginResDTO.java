package org.solcation.solcation_be.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Schema(name = "로그인 요청 DTO")
@Getter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class LoginResDTO {
    //토큰 타입: Bearer
    private String tokenType;

    //Access Token
    private String accessToken;

    //유효기간
    private long expiresIn;

    //사용자 id
    private String userId;

    //사용자 이름
    private String userName;

    //사용자 이메일
    private String email;

    //사용자 전화번호
    private String tel;
}
