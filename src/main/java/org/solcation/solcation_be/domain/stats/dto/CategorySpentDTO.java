package org.solcation.solcation_be.domain.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategorySpentDTO {
    private Long tcPk;
    private String tcName;
    private String tcCode;
    private long totalAmount;
}
