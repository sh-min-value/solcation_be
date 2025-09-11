package org.solcation.solcation_be.domain.wallet.transaction.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateMemoReqDTO {
    @NotNull
    private Long satPk;

    @Size(max = 50, message = "50자 이하로 작성해주세요.")
    private String memo;
}
