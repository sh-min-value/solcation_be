package org.solcation.solcation_be.domain.main.dto;

import lombok.*;
import org.solcation.solcation_be.entity.User;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreviewDTO {
    private String acName;
    private String groupName;
    private String groupLeader;
}