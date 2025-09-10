package org.solcation.solcation_be.domain.wallet.card;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public void OpenCard(@PathVariable("groupId") Long groupId, @RequestBody OpenCardReqDTO dto, @AuthenticationPrincipal JwtPrincipal principal) {
        cardService.openCard(groupId, principal, dto);
    }

    /* 카드 정보 렌더링 */
    @Operation(summary = "카드 정보 렌더링")
    @GetMapping("/info")
    public void getCardInfo(@PathVariable("groupId") Long groupId) {

    }

    /* 카드 거래 내역 렌더링(필터링 포함) */
    @Operation(summary = "카드 거래 내역 렌더링(필터링 포함)")
    @GetMapping("/transactions")
    public void getCardTransactionsByFiltering(@PathVariable("groupId") Long groupId) {

    }
}
