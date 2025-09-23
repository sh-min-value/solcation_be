package org.solcation.solcation_be.domain.wallet.card;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.wallet.card.dto.CardInfoDTO;
import org.solcation.solcation_be.domain.wallet.card.dto.OpenCardReqDTO;
import org.solcation.solcation_be.domain.wallet.card.dto.UserAddressDTO;
import org.solcation.solcation_be.domain.wallet.card.dto.VerifyPwReqDTO;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카드 컨트롤러")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/group/{groupId:\\d+}/account/card/")
public class CardController {
    private final CardService cardService;

    @Operation(summary = "카드 개설")
    @PostMapping("/open")
    public void OpenCard(@PathVariable("groupId") Long groupId, @Valid @RequestBody OpenCardReqDTO dto, @AuthenticationPrincipal JwtPrincipal principal) {
        cardService.openCard(groupId, principal, dto);
    }

    @Operation(summary = "유저 주소 조회")
    @GetMapping("/address")
    public UserAddressDTO getUserAddress(@PathVariable("groupId") Long groupId, @AuthenticationPrincipal JwtPrincipal principal){
        UserAddressDTO dto = cardService.getUserAddress(principal);
        return dto;
    }

    /* 카드 정보 렌더링 */
    @Operation(summary = "카드 정보 렌더링")
    @GetMapping("/{sacPk:\\d+}/info")
    public CardInfoDTO getCardInfo(@PathVariable("groupId") Long groupId, @PathVariable("sacPk") Long sacPk, @AuthenticationPrincipal JwtPrincipal principal) {
        return cardService.getCardInfo(groupId, principal, sacPk);
    }

    /* 카드 해지 */
    @Operation(summary = "카드 해지")
    @PostMapping("/{sacPk:\\d+}/cancel")
    public void cancelCard(@PathVariable("groupId") Long groupId, @PathVariable("sacPk") Long sacPk, @AuthenticationPrincipal JwtPrincipal principal) {
        cardService.cancelCard(groupId, principal, sacPk);
    }

    /* 카드 비밀번호 확인 */
    @Operation(summary = "카드 비밀번호 확인")
    @PostMapping("/{sacPk:\\d+}/verify")
    public boolean verifyPw(@PathVariable("groupId") Long groupId, @PathVariable("sacPk") Long sacPk, @Valid @RequestBody(required = true) VerifyPwReqDTO pw) {
        return cardService.verifyPw(sacPk, pw.getSacPw());
    }
}
