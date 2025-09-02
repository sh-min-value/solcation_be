package org.solcation.solcation_be.domain.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.auth.dto.LoginReqDTO;
import org.solcation.solcation_be.domain.auth.dto.LoginResDTO;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증/인가 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @Operation(description = "로그인")
    @PostMapping("/login")
    public LoginResDTO login(@Valid @RequestBody LoginReqDTO reqDTO) {
        return authService.login(reqDTO);
    }
}