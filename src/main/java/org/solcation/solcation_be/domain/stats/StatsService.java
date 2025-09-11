package org.solcation.solcation_be.domain.stats;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.domain.stats.dto.TravelSpendCompareDTO;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
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
                range.end(),
                TRANSACTIONTYPE.DEPOSIT
        );
    }

    // 다른 그룹과 비교
//    public TravelSpendCompareDTO getCompareTravelSpend(Long tpPk) {
//        Long our = transactionRepository.getOurPerPersonPerDay(tpPk);
//        Long others = transactionRepository.getOthersPerPersonPerDay(tpPk);
//
//        if (our == null) our = 0L;
//        if (others == null) others = 0L;
//
//        Long diff = our - others;
//
//        return new TravelSpendCompareDTO(our, others, diff);
//    }

}