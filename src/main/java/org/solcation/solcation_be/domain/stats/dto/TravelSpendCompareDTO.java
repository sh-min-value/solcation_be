package org.solcation.solcation_be.domain.stats.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TravelSpendCompareDTO {
    private Long ourPayPerDay;
    private Long averagePayPerDay;
    private Long difference;
}
