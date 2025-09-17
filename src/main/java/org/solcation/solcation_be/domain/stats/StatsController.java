package org.solcation.solcation_be.domain.stats;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<CategorySpentDTO> getTravelSpentCategories(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getCategorySpentByTravel(travelId);
    }

    @Operation(summary = "특정 여행의 소비 총계 조회")
    @GetMapping("/{travelId}/total-spent")
    public long getTravelTotalSpent(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelTotalSpent(travelId);
    }

    @Operation(summary = "특정 여행 계획의 예산 총계 조회")
    @GetMapping("/{travelId}/plan-spent")
    public long getTravelPlanSpent(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getTravelPlannedTotal(travelId);
    }

    @Operation(summary = "같은 여행지의 다른 그룹과 소비 총계 비교")
    @GetMapping("/{travelId}/compare")
    public TravelSpendCompareDTO getTravelSpendCompare(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getCompareTravelSpend(travelId);
    }

    @Operation(summary = "같은 여행지의 다른 여행들과 카테고리별 소비 비교")
    @GetMapping("/{travelId}/category-compare")
    public List<CategorySpentCompareDTO> getCategoryComparison(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getCategoryCompare(travelId);
    }

    @Operation(summary = "여행 계획 상 소비와 실제 소비 카테고리 별 비교")
    @GetMapping("/{travelId}/plan-actual-compare")
    public List<CategoryPlannedCompareDTO> getPlanActualComparison(@PathVariable Long groupId, @PathVariable Long travelId) {
        return statsService.getPlanActualComparison(travelId);
    }

    @Operation(summary = "Gemini 인사이트 출력")
    @GetMapping("/api/{travelId}/insight")
    public ResponseEntity<InsightDTO> getTravelInsight(@PathVariable Long groupId, @PathVariable Long travelId) {
        String msg = statsService.generateTravelInsight(travelId);
        return ResponseEntity.ok(new InsightDTO(msg, travelId));
    }

}
