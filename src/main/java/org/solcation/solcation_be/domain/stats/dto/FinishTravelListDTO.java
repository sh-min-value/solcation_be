package org.solcation.solcation_be.domain.stats.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class FinishTravelListDTO {
    private String tpTitle;
    private String tpLocation;
    private LocalDate tpStart;
    private LocalDate tpEnd;
    private String tpImage;
    private String tpcIcon;
}
