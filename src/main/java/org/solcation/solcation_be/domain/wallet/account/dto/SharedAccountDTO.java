package org.solcation.solcation_be.domain.wallet.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

import jakarta.persistence.Convert;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.converter.DepositCycleConverter;
import org.solcation.solcation_be.entity.converter.DepositDayConverter;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;

@Schema(name = "모임통장 계좌 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SharedAccountDTO {
    @Schema(description = "계좌 PK")
    @NotBlank
    private Long saPk;

    @Schema(description = "그룹 PK", example = "13")
    @NotNull
    private Long groupPk;

    @Schema(description = "초기 잔액", example = "100000")
    private int balance;

    @Schema(description = "입금 알람 여부", example = "true")
    private Boolean depositAlarm;

    @Schema(description = "입금 주기", example = "MONTHLY")
    @Convert(converter = DepositCycleConverter.class)
    private DEPOSITCYCLE depositCycle;

    @Schema(description = "입금 예정일", example = "2025-10-01T10:00:00")
    private LocalDateTime depositDate;

    @Schema(description = "입금 요일/일자", example = "MON")
    @Convert(converter = DepositDayConverter.class)
    private DEPOSITDAY depositDay;

    @Schema(description = "입금 금액")
    private int depositAmount;

    @Schema(description = "계좌번호")
    @NotBlank
    @Size(max = 20)
    private String accountNum;

    @Schema(description = "계좌 비밀번호 (6자리)")
    @NotBlank
    @Size(min = 6, max = 6)
    private String saPw;

}
