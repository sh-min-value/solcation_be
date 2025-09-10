package org.solcation.solcation_be.domain.wallet.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Schema(name = "카드 개설 요청 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class OpenCardReqDTO {
    @NotNull
    private String pw;
}
