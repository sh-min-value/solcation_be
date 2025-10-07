package org.solcation.solcation_be.domain.wallet.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Convert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.solcation.solcation_be.entity.converter.DepositCycleConverter;
import org.solcation.solcation_be.entity.converter.DepositDayConverter;
import org.solcation.solcation_be.entity.enums.DEPOSITCYCLE;
import org.solcation.solcation_be.entity.enums.DEPOSITDAY;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Schema(name = "모임통장 계좌 DTO")
@Getter
@Builder
@AllArgsConstructor
public class SharedAccountReqDTO {
    @Schema(description = "그룹 PK", example = "13")
    @NotNull
    private Long groupPk;

    @Schema(description = "계좌 비밀번호 (6자리)")
    @NotBlank
    @Size(min = 6, max = 6)
    private String saPw;

    @Schema(description = "서명")
    @NotNull
    @Valid
    private MultipartFile signature;
}
