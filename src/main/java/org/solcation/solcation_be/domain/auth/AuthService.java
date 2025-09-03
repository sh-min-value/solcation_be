package org.solcation.solcation_be.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.auth.dto.LoginReqDTO;
import org.solcation.solcation_be.domain.auth.dto.LoginResDTO;
import org.solcation.solcation_be.domain.auth.dto.SignupReqDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /* 회원가입 */
    @Transactional
    public void signUp(SignupReqDTO req) {
        User user = SignupReqDTO.toEntity(req);
        user.setPwEncoding(passwordEncoder.encode(req.getUserPw()));
        userRepository.save(user);
    }

    /* 로그인 */
    @Transactional
    public LoginResDTO login(LoginReqDTO req) {
        //아이디 조회
        User user = userRepository.findByUserId(req.getUserId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        //비밀번호 일치 여부 확인
        if(!passwordEncoder.matches(req.getUserPw(), user.getUserPw())) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        //jwt 발급
        String roleName = user.getRole() != null ? user.getRole().name() : "USER";
        var roles = List.of("ROLE_" + roleName);

        String newAccess = jwtTokenProvider.createAccessToken(
                String.valueOf(user.getUserId()),
                Map.of(
                        "userPk", user.getUserPk(),
                        "roles", roles,
                        "userId", user.getUserId(),
                        "userName", user.getUserName(),
                        "email", user.getEmail(),
                        "tel", user.getTel()
                )
        );

        return LoginResDTO.builder()
                .tokenType("Bearer")
                .accessToken(newAccess)
                .expiresIn(jwtTokenProvider.getAccessValidityMs())
                .userId(user.getUserId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .tel(user.getTel())
                .build();
    }
}
