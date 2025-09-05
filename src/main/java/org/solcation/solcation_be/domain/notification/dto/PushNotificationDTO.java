package org.solcation.solcation_be.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.solcation.solcation_be.entity.AlarmCategory;
import org.solcation.solcation_be.entity.Group;

import java.time.LocalDateTime;

@Schema(name = "알림 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationDTO {
    @NotNull
    private Long pnPk;

    @NotNull
    private String title;

    @NotNull
    private LocalDateTime pnTime;

    @NotNull
    private String content;

    @NotNull
    private String acDest;

    @NotNull
    private String groupName;

    @NotNull
    private String groupImage;

    private Boolean isAccepted;

    private LocalDateTime readAt;
}
