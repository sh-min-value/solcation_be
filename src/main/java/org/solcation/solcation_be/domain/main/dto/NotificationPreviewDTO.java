package org.solcation.solcation_be.domain.main.dto;

import lombok.*;

@Getter
@Builder
public class NotificationPreviewDTO {
    private String acCode;
    private String groupName;
    private String groupLeader;
}