package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.domain.stats.dto.TravelSpentStatsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups/{groupId:\\d+}/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/finished-travels")
    public List<FinishTravelListDTO> getFinishedTravels(@PathVariable Long groupId) {
        return statsService.getFinishedTravels(groupId);
    }

    @GetMapping("/{travelId}/category-spent")
    public List<TravelSpentStatsDTO> getTravelSpentCategories(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelSpentStats(travelId);
    }

    @GetMapping("/{travelId}/total-spent")
    public int getTravelTotalSpent(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelTotalSpent(travelId);
    }

    @GetMapping("/{travelId}/compare")
    public Map<String, Integer> getPerPersonPerDayCompare(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getPerPersonPerDayCompare(travelId);
    }
}
