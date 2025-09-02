package org.solcation.solcation_be.domain.main.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupShortDTO {
    private Long groupPk;
    private String groupName;
    private String groupImage;
}
