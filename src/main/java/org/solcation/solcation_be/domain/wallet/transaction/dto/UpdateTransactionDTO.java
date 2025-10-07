package org.solcation.solcation_be.domain.wallet.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;

@Getter
public class UpdateTransactionDTO {
    @NotNull
    private Long satPk;

    @Size(max = 50, message = "50자 이하로 작성해주세요.")
    private String memo;

    @NotNull
    private TRANSACTIONCODE tcPk;
}
