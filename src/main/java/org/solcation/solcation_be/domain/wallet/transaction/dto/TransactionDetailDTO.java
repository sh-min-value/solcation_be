package org.solcation.solcation_be.domain.wallet.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.common.annotation.KstDateTime;
import org.solcation.solcation_be.entity.GroupMember;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;

import java.time.Instant;

@Schema(name = "이용 내역 상세 DTO")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDetailDTO {
    //pk
    @NotNull
    private Long satPk;

    //카테고리
    private TransactionCategory tcPk;

    //일시
    @NotNull
    @KstDateTime
    private Instant satTime;

    //적요
    @NotNull
    private String briefs;

    //거래 유형
    @NotNull
    private TRANSACTIONTYPE tType;

    //거래한 모임원(이름)
    @NotNull
    private String groupMember;

    //입금처
    private String depositDestination;

    //출금처
    private String withdrawDestination;

    //거래 후 잔액
    @NotNull
    private int balance;

    //거래액
    @NotNull
    private int satAmount;

    //메모
    private String memo;
}
