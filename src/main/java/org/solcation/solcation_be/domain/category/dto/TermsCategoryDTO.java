package org.solcation.solcation_be.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class TermsCategoryDTO {
    private Long termsPk;
    private String termsCode;
}
