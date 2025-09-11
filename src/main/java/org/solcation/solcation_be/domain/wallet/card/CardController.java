package org.solcation.solcation_be.domain.wallet.card;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.wallet.card.dto.CardInfoDTO;
import org.solcation.solcation_be.domain.wallet.card.dto.OpenCardReqDTO;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "카드 컨트롤러")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("group/{groupId:\\d+}/card")
public class CardController {
    private final CardService cardService;

    @Operation(summary = "카드 개설")
    @PostMapping("/open")
    public void OpenCard(@PathVariable("groupId") Long groupId, @Valid @RequestBody OpenCardReqDTO dto, @AuthenticationPrincipal JwtPrincipal principal) {
        cardService.openCard(groupId, principal, dto);
    }

    /* 카드 정보 렌더링 */
    @Operation(summary = "카드 정보 렌더링")
    @GetMapping("/info")
    public CardInfoDTO getCardInfo(@PathVariable("groupId") Long groupId, @AuthenticationPrincipal JwtPrincipal principal) {
        return cardService.getCardInfo(groupId, principal);
    }

    /* 카드 해지 */
    @Operation(summary = "카드 해지")
    @PostMapping("/cancel")
    public void cancelCard(@PathVariable("groupId") Long groupId, @AuthenticationPrincipal JwtPrincipal principal) {
        cardService.cancelCard(groupId, principal);
    }
}
