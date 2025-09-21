package org.solcation.solcation_be.domain.category.dto;

import lombok.*;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class TravelPlanCategoryDTO {
    private Long tpcPk;
    private String tpcName;
    private String tpcIcon;
    private String tpcCode;
}
