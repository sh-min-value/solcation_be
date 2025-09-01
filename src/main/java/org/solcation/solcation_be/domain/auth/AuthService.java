package org.solcation.solcation_be.domain.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.auth.dto.SignupReqDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /* 회원가입 */
    public void signUp(SignupReqDTO req) {
        User user = SignupReqDTO.toEntity(req);
        user.setPwEncoding(passwordEncoder.encode(req.getUserPw()));
        userRepository.save(user);
    }
}
