package org.solcation.solcation_be.domain.wallet.transaction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "거래 카테고리 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class TransactionCategoryDTO {
    @NotNull
    private Long tcPk;

    @NotNull
    private String tcName;

    @NotNull
    private String tcIcon; //파일 경로

    @NotNull
    private String tcCode;
}
