package org.solcation.solcation_be.domain.wallet.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.common.annotation.KstDateTime;

import java.time.Instant;

@Schema(name = "거래 내역 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionDTO {
    //일시
    @NotNull
    @KstDateTime
    private Instant satTime;

    //적요
    @NotNull
    private String briefs;

    //카테고리
    @NotNull
    private String tcName;

    //거래 유형
    @NotNull
    private String tType;

    //거래액
    @NotNull
    private int satAmount;

    //잔액
    @NotNull
    private int balance;
}
