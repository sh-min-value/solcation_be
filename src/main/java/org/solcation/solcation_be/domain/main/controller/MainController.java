package org.solcation.solcation_be.domain.main.controller;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.main.dto.GroupShortDTO;
import org.solcation.solcation_be.domain.main.service.GroupShortService;
import org.springframework.http.ResponseEntity;
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
            @AuthenticationPrincipal String userId,
            @RequestParam(defaultValue = "8") int limit
    ) {
        Long userPk = 1L;
        return groupShortService.getUserGroups(userPk, limit);
    }
}

