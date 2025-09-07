package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.domain.stats.dto.TravelSpentStatsDTO;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TravelRepository travelRepository;
    private final PlanDetailRepository planDetailRepository;

    // 완료된 여행 목록 조회
    public List<FinishTravelListDTO> getFinishedTravels(Long groupPk) {
        List<Travel> travels =
                travelRepository.findByGroup_GroupPkAndTpStateOrderByTpEndDesc(groupPk, TRAVELSTATE.FINISH);

        return travels.stream()
                .map(t -> FinishTravelListDTO.builder()
                        .tpTitle(t.getTpTitle())
                        .tpLocation(t.getTpLocation())
                        .tpStart(t.getTpStart())
                        .tpEnd(t.getTpEnd())
                        .tpImage(t.getTpImage())
                        .tpcIcon(t.getTravelCategory().getTpcIcon())
                        .build())
                .toList();
    }

    // 카테고리 별 합계
    public List<TravelSpentStatsDTO> getTravelSpentStats(Long travelId) {

        List<PlanDetail> details = planDetailRepository.findAliveByTravelOrderByPosition(travelId);

        Map<String, Integer> byCategory = details.stream()
                .collect(Collectors.groupingBy(
                        d -> d.getTransactionCategory().getTcName(),
                        Collectors.summingInt(PlanDetail::getPdCost)
                ));

        return byCategory.entrySet().stream()
                .map(e -> TravelSpentStatsDTO.builder()
                        .pdCost(e.getValue())
                        .tcName(e.getKey())
                        .build())
                .toList();
    }

    // 소비 총계
    public int getTravelTotalSpent(Long travelId) {
        List<PlanDetail> details = planDetailRepository.findAliveByTravelOrderByPosition(travelId);
        return details.stream().mapToInt(PlanDetail::getPdCost).sum();
    }
}
