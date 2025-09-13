package org.solcation.solcation_be.domain.stats.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryPlannedCompareDTO {
    private Long tcPk;
    private String tcName;
    private String tcCode;
    private long plannedAmount;
    private long actualAmount;
    private long diff;
}
