package org.solcation.solcation_be.domain.wallet.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class UpdateCategoryReqDTO {
    @NotNull
    private Long satPk;

    @NotNull
    private Long tcPk;
}
