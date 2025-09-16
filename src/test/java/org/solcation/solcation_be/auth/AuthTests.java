package org.solcation.solcation_be.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.auth.AuthService;
import org.solcation.solcation_be.domain.auth.dto.LoginReqDTO;
import org.solcation.solcation_be.domain.auth.dto.LoginResDTO;
import org.solcation.solcation_be.domain.auth.dto.SignupReqDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
public class AuthTests {
    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void signupTest() {
        SignupReqDTO req = SignupReqDTO.builder()
                .userId("user1")
                .userPw("1234")
                .streetAddr("서울 쌍문동")
                .addrDetail("쌍문역")
                .postalCode("12909")
                .tel("01031037896")
                .userName("최세창")
                .dateOfBirth(LocalDate.now())
                .gender("m")
                .email("user1@gmail.com")
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

    @Test
    public void getUserTest() {
        User user = userRepository.findByUserId("admin3").orElseThrow(() -> new RuntimeException("user not found"));
        log.info("user - 주소: {}", user.getStreetAddr());
    }
}
