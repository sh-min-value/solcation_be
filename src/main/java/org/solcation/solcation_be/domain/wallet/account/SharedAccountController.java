package org.solcation.solcation_be.domain.wallet.account;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.wallet.account.dto.*;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "모임통장 계좌 컨트롤러")
@RequiredArgsConstructor
@RestController
@RequestMapping("/group/{groupId:\\d+}/account")
public class SharedAccountController {
    private final SharedAccountService sharedAccountService;

    @GetMapping("/info")
    public SharedAccountResDTO getSharedAccount(@PathVariable long groupId, @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
        return sharedAccountService.getSharedAccountInfo(groupId, jwtPrincipal);
    }

    @PostMapping(value="/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void createSharedAccount(@PathVariable long groupId,
                                                   @Valid @ModelAttribute SharedAccountReqDTO dto) {
        sharedAccountService.createSharedAccount(groupId, dto);
    }

    @PostMapping("/cycle")
    public void updateDepositCycle(@PathVariable long groupId, @RequestBody DepositCycleDTO dto) {
        sharedAccountService.updateDepositCycle(groupId, dto);
    }

    @PostMapping("/reset-cycle/{saPk}")
    public void disableDepositCycle(@PathVariable long groupId, @PathVariable long saPk) {
        sharedAccountService.disableDepositCycle(saPk);
    }

    @Operation(summary = "모임통장 페이지 비밀번호 확인")
    @PostMapping("/{saPk}/sa-login")
    public ResponseEntity<SharedAccountLoginResDTO> loginSharedAccount(@PathVariable Long groupId, @PathVariable Long saPk, @RequestBody @Valid SharedAccountLoginReqDTO req) {
        SharedAccountLoginResDTO res = sharedAccountService.loginSharedAccount(groupId, saPk, req.getSaPw());
        return ResponseEntity.ok(res);
    }
}
