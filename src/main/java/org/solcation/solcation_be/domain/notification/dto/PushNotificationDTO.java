package org.solcation.solcation_be.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.solcation.solcation_be.common.annotation.KstDateTime;


import java.time.Instant;

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
    @KstDateTime
    private Instant pnTime;

    @NotNull
    private String content;

    @NotNull
    private String acDest;

    @NotNull
    private Long groupPk;

    @NotNull
    private String groupName;

    @NotNull
    private String groupImage;

    private Boolean isAccepted;

    @KstDateTime
    private Instant readAt;
}
