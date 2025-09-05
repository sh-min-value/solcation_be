package org.solcation.solcation_be.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Schema(name = "Redis publish message용 DTO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PublishDTO {
    @NotNull
    private Long pnPk;

    @NotNull
    private Long userPk;
}
