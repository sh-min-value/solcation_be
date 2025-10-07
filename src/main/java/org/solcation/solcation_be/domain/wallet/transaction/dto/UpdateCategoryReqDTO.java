package org.solcation.solcation_be.domain.wallet.transaction.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;

@Getter
public class UpdateCategoryReqDTO {
    @NotNull
    private Long satPk;

    @NotNull
    private TRANSACTIONCODE tcPk;
}
