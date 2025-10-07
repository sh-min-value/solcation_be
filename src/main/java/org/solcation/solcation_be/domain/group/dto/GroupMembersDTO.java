package org.solcation.solcation_be.domain.group.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.solcation.solcation_be.entity.User;

import java.util.List;

@Schema(name = "그룹 참여자 정보 DTO")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupMembersDTO {
    //개설자
    @NotNull
    private GroupMemberDTO groupLeader;

    //참여자 리스트
    @NotNull
    private List<GroupMemberDTO> members;

    //대기자 리스트
    @NotNull
    private List<GroupMemberDTO> waitingList;
}
