package org.solcation.solcation_be.domain.wallet.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.common.annotation.KstDateTime;

import java.time.Instant;

@Schema(name = "카드 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoDTO {
    //카드 번호
    @NotNull
    private String cardNum;

    //이번달 이용 금액
    @NotNull
    private Long totalCost;

    //카드 최초 생성일
    @KstDateTime
    @NotNull
    private Instant createdAt;
}
