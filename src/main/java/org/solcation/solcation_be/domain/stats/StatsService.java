package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.domain.stats.dto.TravelSpendCompareDTO;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
                .orElseThrow(() -> new IllegalArgumentException("invalid travel id: " + tpPk));
        LocalDateTime start = travel.getTpStart().atStartOfDay();
        LocalDateTime end = travel.getTpEnd().atTime(23, 59, 59);
        return transactionRepository.sumSpentTravel(tpPk, List.of(TRANSACTIONTYPE.WITHDRAW, TRANSACTIONTYPE.CARD), start, end);
    }

    // 계획 상 소비 총계
    public long getTravelPlannedTotal(Long tpPk) {
        return transactionRepository.sumPlannedTravel(tpPk);
    }

    // 카테고리 별 합계
    public List<CategorySpentDTO> getCategorySpentByTravel(Long travelId) {
        Travel travel = travelRepository.findById(travelId)
                .orElseThrow(() -> new IllegalArgumentException("해당 여행 없음: " + travelId));

        LocalDateTime start = travel.getTpStart().atStartOfDay();
        LocalDateTime end = travel.getTpEnd().atTime(23, 59, 59);
        return transactionRepository.categorySpent(start, end);
    }

    // 다른 그룹과 비교
    public TravelSpendCompareDTO getCompareTravelSpend(Long tpPk) {
        Object[] raw = transactionRepository.compareTravelSpend(tpPk);
        if (raw == null) {
            return new TravelSpendCompareDTO(0L, 0L, 0L);
        }

        Object[] row = raw;

        if (row.length == 1 && row[0] instanceof Object[]) {
            row = (Object[]) row[0];
        }

        if (row.length < 3) {
            return new TravelSpendCompareDTO(0L, 0L, 0L);
        }

        Long our = toLong(row[0]);
        Long others = toLong(row[1]);
        Long diff = toLong(row[2]);

        return new TravelSpendCompareDTO(our, others, diff);
    }

    // 네이티브 쿼리 결과 long으로 통일
    private Long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        if (v instanceof Object[] arr && arr.length > 0 && arr[0] instanceof Number) {
            return ((Number) arr[0]).longValue();
        } try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return 0L;
        }
    }

}