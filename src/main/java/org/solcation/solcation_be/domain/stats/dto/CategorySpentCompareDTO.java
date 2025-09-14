package org.solcation.solcation_be.domain.stats.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySpentCompareDTO {
    private Long tcPk;
    private String tcName;
    private String tcCode;
    private long myAmount;
    private long othersAvg;
    private long diff;
}
