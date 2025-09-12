package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.stats.dto.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final TravelRepository travelRepository;
    private final TransactionRepository transactionRepository;

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

    // 실제 소비 총계
    public long getTravelTotalSpent(Long tpPk) {
        Travel travel = travelRepository.findById(tpPk)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        return transactionRepository.sumSpentTravel(
                tpPk,
                List.of(TRANSACTIONTYPE.WITHDRAW, TRANSACTIONTYPE.CARD),
                range.start(),
                range.end()
        );
    }

    // 계획 상 소비 총계
    public long getTravelPlannedTotal(Long tpPk) {
        return transactionRepository.sumPlannedTravel(tpPk);
    }

    // 카테고리 별 합계
    public List<CategorySpentDTO> getCategorySpentByTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        return transactionRepository.categorySpent(
                range.start(),
                range.end()
        );
    }

    // 다른 그룹과 비교
    public TravelSpendCompareDTO getCompareTravelSpend(Long tpPk) {
        Travel tp = travelRepository.findById(tpPk).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));
        ZonedTimeRange range = ZonedTimeUtil.custom(tp.getTpStart(), tp.getTpEnd());
        Instant start = range.start();
        Instant endExclusive = range.end();

        List<TRANSACTIONTYPE> types = List.of(TRANSACTIONTYPE.WITHDRAW, TRANSACTIONTYPE.CARD);

        long ourTotal = transactionRepository.sumSpentTravel(tpPk, types, start, endExclusive);
        long days = ChronoUnit.DAYS.between(tp.getTpStart(), tp.getTpEnd()) + 1;
        long ourDenominator = Math.max(1, tp.getParticipant() * Math.max(1, days));
        long ourPerPersonPerDay = ourTotal / ourDenominator;

        long othersTotal = transactionRepository.sumOthersSpentBySameLocation(tpPk, types);
        long othersPersonDays = transactionRepository.sumOthersPersonDays(tpPk);
        long othersPerPersonPerDay = othersPersonDays > 0 ? (othersTotal / othersPersonDays) : 0;

        long diff = ourPerPersonPerDay - othersPerPersonPerDay;
        return new TravelSpendCompareDTO(ourPerPersonPerDay, othersPerPersonPerDay, diff);
    }

    // 다른 그룹과 카테고리 별 비교
    public List<CategorySpentCompareDTO> getCategoryCompare(Long tpPk) {
        Travel travel = travelRepository.findById(tpPk)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        // 내 여행 카테고리 소비
        List<CategorySpentDTO> mine = transactionRepository.categorySpent(
                range.start(),
                range.end()
        );

        // 다른 여행 평균 소비
        List<Object[]> rows = transactionRepository.categoryOthersAvgPerTravel(tpPk);
        List<CategorySpentDTO> others = rows.stream()
                .map(r -> new CategorySpentDTO(
                        ((Number) r[0]).longValue(),
                        (String) r[1],
                        (String) r[2],
                        ((Number) r[3]).longValue()
                ))
                .toList();

        Map<Long, CategorySpentDTO> mineMap = mine.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, dto -> dto));
        Map<Long, CategorySpentDTO> othersMap = others.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, dto -> dto));

        List<CategorySpentCompareDTO> result = new ArrayList<>();
        for (Long tcPk : mineMap.keySet()) {
            CategorySpentDTO m = mineMap.get(tcPk);
            CategorySpentDTO o = othersMap.get(tcPk);
            long myAmount = m != null ? m.getTotalAmount() : 0L;
            long othersAvg = o != null ? o.getTotalAmount() : 0L;
            long diff = myAmount - othersAvg;
            result.add(CategorySpentCompareDTO.builder()
                    .tcPk(tcPk)
                    .tcName(m != null ? m.getTcName() : o.getTcName())
                    .tcCode(m != null ? m.getTcCode() : o.getTcCode())
                    .myAmount(myAmount)
                    .othersAvg(othersAvg)
                    .diff(diff)
                    .build());
        }
        return result;
    }

    // 여행 계획과 실제 소비 카테고리 별 비교
    public List<CategoryPlannedCompareDTO> getPlanActualComparison(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PLAN));

        ZonedTimeRange range = ZonedTimeUtil.custom(travel.getTpStart(), travel.getTpEnd());

        List<CategorySpentDTO> planned = transactionRepository.plannedCategorySpentOfTravel(travelId);
        List<CategorySpentDTO> actual = transactionRepository.categorySpent(range.start(), range.end());

        Map<Long, CategorySpentDTO> plannedMap = planned.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, x -> x));
        Map<Long, CategorySpentDTO> actualMap = actual.stream()
                .collect(Collectors.toMap(CategorySpentDTO::getTcPk, x -> x));

        List<Long> keys = Stream.concat(plannedMap.keySet().stream(), actualMap.keySet().stream())
                .distinct()
                .sorted()
                .toList();

        List<CategoryPlannedCompareDTO> result = new ArrayList<>();
        for (Long k : keys) {
            CategorySpentDTO p = plannedMap.get(k);
            CategorySpentDTO a = actualMap.get(k);
            long plannedAmount = p != null ? p.getTotalAmount() : 0L;
            long actualAmount = a != null ? a.getTotalAmount() : 0L;
            long diff = actualAmount - plannedAmount;
            String name = p != null ? p.getTcName() : (a != null ? a.getTcName() : null);
            String code = p != null ? p.getTcCode() : (a != null ? a.getTcCode() : null);
            result.add(CategoryPlannedCompareDTO.builder()
                    .tcPk(k)
                    .tcName(name)
                    .tcCode(code)
                    .plannedAmount(plannedAmount)
                    .actualAmount(actualAmount)
                    .diff(diff)
                    .build());
        }
        return result;
    }
}