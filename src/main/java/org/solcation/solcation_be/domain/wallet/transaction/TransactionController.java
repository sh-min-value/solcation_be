package org.solcation.solcation_be.domain.wallet.transaction;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionDTO;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.util.List;

@Tag(name = "거래 내역 컨트롤러")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("group/{groupId:\\d+}/transaction")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping("/all")
    public List<TransactionDTO> getTransactionsAll(@PathVariable("groupId") Long groupId, @AuthenticationPrincipal JwtPrincipal principal, @PathParam("tType") @Nullable TRANSACTIONTYPE tType) {
        return transactionService.getTransactionsAll(groupId, principal, tType);
    }

    @GetMapping("/card")
    public List<TransactionDTO> getTransactionsCard(@PathVariable("groupId") Long groupId, @AuthenticationPrincipal JwtPrincipal principal, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth) {
        return transactionService.getTransactionsCard(groupId, principal, yearMonth);
    }
}
