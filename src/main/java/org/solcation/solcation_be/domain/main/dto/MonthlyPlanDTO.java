package org.solcation.solcation_be.domain.main.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MonthlyPlanDTO {
    private LocalDate tpStart;
    private LocalDate tpEnd;
    private String gcCode;
    private String tpTitle;
    private String groupName;
}