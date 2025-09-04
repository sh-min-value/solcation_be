package org.solcation.solcation_be.domain.group;

import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.group.dto.AddGroupReqDTO;
import org.solcation.solcation_be.domain.group.dto.GroupInfoDTO;
import org.solcation.solcation_be.domain.group.dto.GroupListDTO;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/group")
public class GroupController {
    private final UserRepository userRepository;
    private final GroupService groupService;

    @PostMapping(value = "/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public boolean addGroup(@ModelAttribute AddGroupReqDTO addGroupReqDTO, @AuthenticationPrincipal JwtPrincipal user){
        User groupLeader = userRepository.findByUserId(user.userId()).orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));
        return groupService.addGroup(addGroupReqDTO, groupLeader);
    }

    @GetMapping("/list")
    public List<GroupListDTO> getList(@AuthenticationPrincipal JwtPrincipal user, @RequestParam(required = false) String searchTerm) {
        return groupService.getList(user.userId(), searchTerm);
    }

    @GetMapping("/{groupId:\\d+}/get")
    public GroupInfoDTO groupMain(@PathVariable("groupId") Long groupId) {
        return groupService.getGroupInfo(groupId);
    }
}
