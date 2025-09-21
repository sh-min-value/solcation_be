package org.solcation.solcation_be.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
public class GroupCategoryDTO {
    Long gcPk;
    String gcName;
    String gcIcon;
    String gcCode;
}
