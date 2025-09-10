package org.solcation.solcation_be.domain.wallet.card.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;
import java.util.List;

@Schema(name = "카드 거래 내역 요청 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CardTransactionsReqDTO {
    //거래 내역 시점
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM", timezone = "Asia/Seoul")
    private YearMonth yearMonth;
}
