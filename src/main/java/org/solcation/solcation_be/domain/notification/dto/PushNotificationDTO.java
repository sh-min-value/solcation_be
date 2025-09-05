package org.solcation.solcation_be.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
    private String acName;

    @NotNull
    private String acDest;

    @NotNull
    private String content;

    @NotNull
    private Group group;

    @NotNull
    private Boolean isAccepted;
}
