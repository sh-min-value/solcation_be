package org.solcation.solcation_be.domain.wallet.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "카드 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardInfoDTO {
    //카드 번호
    private String cardNum;

    //이번달 이용 금액
    private int totalCost;
}
