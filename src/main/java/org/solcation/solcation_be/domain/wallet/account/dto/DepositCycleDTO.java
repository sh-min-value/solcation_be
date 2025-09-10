package org.solcation.solcation_be.domain.wallet.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;

@Schema(name = "입금 주기 설정 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositCycleDTO {
    @Schema(description = "계좌 PK")
    @NotBlank
    private Long saPk;

    @Schema(description = "입금 알람 여부", example = "true")
    private Boolean depositAlarm;

    @Schema(description = "입금 주기", example = "MONTH")
    private DEPOSITCYCLE depositCycle;

    @Schema(description = "입금 예정일", example = "24")
    private Integer depositDate;

    @Schema(description = "입금 요일/일자", example = "MON")
    private DEPOSITDAY depositDay;

    @Schema(description = "입금 금액")
    private int depositAmount;

}
