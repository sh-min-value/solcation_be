package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.domain.stats.dto.TravelSpentStatsDTO;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.GroupMemberRepository;
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
    private final GroupMemberRepository groupMemberRepository;

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
    public long getTravelTotalSpent(Long travelId) {
        return planDetailRepository.findAliveByTravelOrderByPosition(travelId).stream()
                .map(PlanDetail::getPdCost)
                .filter(java.util.Objects::nonNull)
                .mapToLong(Integer::longValue)
                .sum();
    }

    // 다른 그룹과 비교
    public Map<String, Integer> getPerPersonPerDayCompare(Long travelId) {
        Travel t = travelRepository.findById(travelId).orElseThrow();
        Long groupPk = t.getGroup().getGroupPk();

        long members = groupMemberRepository.countByGroup_GroupPkAndIsAcceptedTrueAndIsOutFalse(groupPk);
        long days = java.time.temporal.ChronoUnit.DAYS.between(t.getTpStart(), t.getTpEnd()) + 1;
        int total = planDetailRepository.findAliveByTravelOrderByPosition(travelId)
                .stream().mapToInt(PlanDetail::getPdCost).sum();
        int our = (members == 0 || days == 0) ? 0 : (int) Math.round((double) total / members / days);

        var others = travelRepository.findByTpLocationAndGroup_GroupPkNotAndTpState(
                t.getTpLocation(),
                groupPk,
                TRAVELSTATE.FINISH
        );

        java.util.DoubleSummaryStatistics stats = others.stream().mapToDouble(o -> {
            long m = groupMemberRepository.countByGroup_GroupPkAndIsAcceptedTrueAndIsOutFalse(o.getGroup().getGroupPk());
            long d = java.time.temporal.ChronoUnit.DAYS.between(o.getTpStart(), o.getTpEnd()) + 1;
            if (m == 0 || d == 0) return 0d;
            int tot = planDetailRepository.findAliveByTravelOrderByPosition(o.getTpPk())
                    .stream().mapToInt(PlanDetail::getPdCost).sum();
            return (double) tot / m / d;
        }).summaryStatistics();

        int otherEverage = stats.getCount() == 0 ? 0 : (int) Math.round(stats.getAverage());
        int diff = our - otherEverage;

        return Map.of(
                "ourPayPerDay", our,
                "averagePerDay", otherEverage,
                "difference", diff
        );
    }
}