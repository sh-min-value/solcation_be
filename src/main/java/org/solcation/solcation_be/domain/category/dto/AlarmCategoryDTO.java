package org.solcation.solcation_be.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class AlarmCategoryDTO {
    private Long acPk;
    private String acCode;
    private String acDest;
}
