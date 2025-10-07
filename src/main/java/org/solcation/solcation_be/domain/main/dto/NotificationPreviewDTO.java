package org.solcation.solcation_be.domain.main.dto;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class NotificationPreviewDTO {
    private String acCode;
    private String groupName;
    private Long groupPk;
    private String groupLeader;
    private String pnTitle;
    private String acDest;
    private Long pnPk;
}