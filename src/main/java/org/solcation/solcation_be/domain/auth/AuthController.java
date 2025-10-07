package org.solcation.solcation_be.domain.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.auth.dto.LoginReqDTO;
import org.solcation.solcation_be.domain.auth.dto.LoginResDTO;
import org.solcation.solcation_be.domain.auth.dto.SignupReqDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증/인가 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "로그인", description = "로그인")
    @PostMapping("/login")
    public LoginResDTO login(@Valid @RequestBody LoginReqDTO reqDTO) {
        return authService.login(reqDTO);
    }

    @Operation(summary = "회원가입", description = "회원가입")
    @PostMapping("/sign-up")
    public void signup(@Valid @RequestBody SignupReqDTO reqDTO) {
        authService.signUp(reqDTO);
    }

    @Operation(summary = "아이디 중복체크")
    @GetMapping("/check-dup")
    public boolean checkDupId(@RequestParam("userId") String userId) {
        return authService.checkIdDup(userId);
    }
}