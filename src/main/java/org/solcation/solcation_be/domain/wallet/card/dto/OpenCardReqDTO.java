package org.solcation.solcation_be.domain.wallet.card.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(min = 6, max = 6, message = "비밀번호는 6자리여야 합니다.")
    private String pw;
}
