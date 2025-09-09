package org.solcation.solcation_be.domain.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Schema(name = "그룹 초대 요청 수락,거절 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class UpdateGroupInviteReqDTO {
    //알림 pk
    @NotNull
    @Schema(description = "알림 pk")
    private Long pnPk;

    //그룹 pk
    @NotNull
    @Schema(description = "그룹 pk")
    private Long groupPk;

    //초대 수락/거절 여부(t/f)
    @NotNull
    @Schema(description = "초대 수락/거절 여부")
    private Boolean decision;
}
