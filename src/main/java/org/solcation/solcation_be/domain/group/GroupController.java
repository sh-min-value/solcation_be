package org.solcation.solcation_be.domain.group;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.*;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "그룹 컨트롤러")
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController {
    private final UserRepository userRepository;
    private final GroupService groupService;

    @Operation(summary = "새 그룹 생성")
    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public boolean addGroup(@ModelAttribute AddGroupReqDTO addGroupReqDTO, @AuthenticationPrincipal JwtPrincipal user){
        User groupLeader = userRepository.findByUserId(user.userId()).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        return groupService.addGroup(addGroupReqDTO, groupLeader);
    }

    @Operation(summary = "그룹 목록 조회")
    @GetMapping("/list")
    public List<GroupListDTO> getList(@AuthenticationPrincipal JwtPrincipal user, @RequestParam(required = false) String searchTerm) {
        return groupService.getList(user.userId(), searchTerm);
    }

    @Operation(summary = "그룹 메인 - 그룹 정보 조회")
    @GetMapping("/{groupId:\\d+}/get")
    public GroupInfoDTO groupMain(@PathVariable("groupId") Long groupId) {
        return groupService.getGroupInfo(groupId);
    }

    @Operation(summary = "그룹 메인 - 참여자 정보 조회")
    @GetMapping("/{groupId:\\d+}/members")
    public GroupMembersDTO getMembers(@PathVariable("groupId") Long groupId) {
        return groupService.getGroupMembers(groupId);
    }

    @Operation(summary = "그룹 메인 - 그룹 초대 전송")
    @PostMapping("/{groupId:\\d+}/invite")
    public void inviteMembers(@PathVariable("groupId") Long groupId, @PathParam("tel") String tel) {
        groupService.inviteMembers(groupId, tel);
    }

    @Operation(summary = "초대자 정보 조회")
    @GetMapping("/{groupId:\\d+}/get-invitee")
    public UserDTO getInvitee(@PathVariable("groupId") Long groupId, @PathParam("tel") String tel) {
        return groupService.getInvitee(groupId, tel);
    }
}
