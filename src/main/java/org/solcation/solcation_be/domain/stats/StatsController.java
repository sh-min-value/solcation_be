package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/groups/{groupId:\\d+}/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/finished-travels")
    public List<FinishTravelListDTO> getFinishedTravels(@PathVariable Long groupId) {
        return statsService.getFinishedTravels(groupId);
    }
}
