package org.solcation.solcation_be.domain.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.domain.stats.dto.TravelSpentStatsDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Tag(name = "통계 컨트롤러")
@RestController
@RequestMapping("/groups/{groupId:\\d+}/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "완료한 여행 조회")
    @GetMapping("/finished-travels")
    public List<FinishTravelListDTO> getFinishedTravels(@PathVariable Long groupId) {
        return statsService.getFinishedTravels(groupId);
    }

    @Operation(summary = "특정 여행의 카테고리 별 소비 통계 조회")
    @GetMapping("/{travelId}/category-spent")
    public List<TravelSpentStatsDTO> getTravelSpentCategories(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelSpentStats(travelId);
    }

    @Operation(summary = "특정 여행의 소비 총계 조회")
    @GetMapping("/{travelId}/total-spent")
    public long getTravelTotalSpent(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelTotalSpent(travelId);
    }

    @Operation(summary = "같은 여행지의 다른 그룹과 소비 총계 비교")
    @GetMapping("/{travelId}/compare")
    public Map<String, Integer> getPerPersonPerDayCompare(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getPerPersonPerDayCompare(travelId);
    }
}
