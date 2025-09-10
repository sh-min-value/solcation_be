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
    private String cardNumber;
}
