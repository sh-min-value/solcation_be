package org.solcation.solcation_be.domain.main.controller;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.domain.main.service.GroupShortService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/main")
@RequiredArgsConstructor
public class MainController {

    private final GroupShortService groupShortService;

    @GetMapping("/my-groups")
    public List<GroupShortDTO> getMyGroups(
            @AuthenticationPrincipal(expression = "userPk") Long userPk
    ) {
        if (userPk == null) userPk = 1L; // 더미
        return groupShortService.getUserGroups(userPk);
    }
}
