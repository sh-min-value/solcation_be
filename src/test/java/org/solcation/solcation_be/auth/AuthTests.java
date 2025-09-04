package org.solcation.solcation_be.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.auth.AuthService;
import org.solcation.solcation_be.domain.auth.dto.LoginReqDTO;
import org.solcation.solcation_be.domain.auth.dto.LoginResDTO;
import org.solcation.solcation_be.domain.auth.dto.SignupReqDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
public class AuthTests {
    @Autowired
    private AuthService authService;

    @Test
    public void signupTest() {
        SignupReqDTO req = SignupReqDTO.builder()
                .userId("admin2")
                .userPw("1234")
                .streetAddr("경기도 수원시 영통구")
                .addrDetail("하늘땅")
                .postalCode("12345")
                .tel("01012345678")
                .userName("홍길동")
                .dateOfBirth(LocalDate.now())
                .gender("m")
                .email("1234@gmail.com")
                .build();

        authService.signUp(req);

        log.info("sing up userId: {}", SignupReqDTO.toEntity(req).getUserId());
    }

    @Test
    public void loginSucTest() {
        LoginReqDTO reqDto = LoginReqDTO.builder()
                .userId("bread0930")
                .userPw("1234")
                .build();

        LoginResDTO resDto = authService.login(reqDto);

        log.info("login successful: {}", resDto.getAccessToken());
    }
}
