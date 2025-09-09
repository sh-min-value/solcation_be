package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import org.solcation.solcation_be.domain.travel.service.EditSessionService;
import org.solcation.solcation_be.domain.travel.service.SnapshotCommitService;
import org.solcation.solcation_be.domain.travel.ws.JoinPayload;

@RestController
@RequiredArgsConstructor
@RequestMapping("/group/{groupId:\\d+}/travel/{travelId:\\d+}")
public class PlanDetailController {

    private final EditSessionService editSessionService;
    private final SnapshotCommitService snapshotCommitService;

    @PostMapping("/edit/join")
    public JoinPayload join(@PathVariable long groupId, @PathVariable long travelId,
                            @RequestParam int day, @RequestParam String userId) {
        return editSessionService.join(travelId, day, userId);
    }

    @PostMapping("/edit/leave")
    public void leave(@PathVariable long groupId, @PathVariable long travelId,
                      @RequestParam int day, @RequestParam String userId) {
        editSessionService.leave(travelId, day, userId);
    }

    @PostMapping("/edit/save")
    public void save(@PathVariable long groupId, @PathVariable long travelId,
                     @RequestParam int day, @RequestParam String clientId) {
        snapshotCommitService.save(travelId, day, clientId);
    }
}
