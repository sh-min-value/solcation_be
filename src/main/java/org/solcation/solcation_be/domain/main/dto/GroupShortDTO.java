package org.solcation.solcation_be.domain.main.dto;

import lombok.*;

@Getter
@Builder
public class GroupShortDTO {
    private Long groupPk;
    private String groupName;
    private String groupImage;
}
