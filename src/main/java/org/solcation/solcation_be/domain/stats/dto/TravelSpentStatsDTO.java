package org.solcation.solcation_be.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TravelSpentStatsDTO {
    private int pdCost;
    private String tcName;
}
