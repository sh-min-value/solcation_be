package org.solcation.solcation_be.domain.travel;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.util.Positioning;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.TransactionCategory;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
import org.solcation.solcation_be.repository.TransactionCategoryRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PlanDetailService {

    private final TravelRepository travelRepository;
    private final PlanDetailRepository planDetailRepository;
    private final TransactionCategoryRepository transactionCategoryRepository;

    /* 조회 */
    @Transactional(readOnly = true)
    public List<PlanDetailDTO> getTravelPlans(Long travelPk) {
        return planDetailRepository.findAliveByTravelOrderByPosition(travelPk)
                .stream().map(PlanDetailDTO::entityToDTO).toList();
    }

    /* 삽입 */
    public PlanDetailDTO insertBetween(Long travelPk, int day,
                                       String prevCrdtId, String nextCrdtId,
                                       String pdPlace, String pdAddress, int pdCost, Long tcPk,
                                       String clientId, Long opTs) {
        // prev/next 보정: "0", "", "null" → null
        prevCrdtId = normalize(prevCrdtId);
        nextCrdtId = normalize(nextCrdtId);

        Travel travel = travelRepository.getReferenceById(travelPk);
        TransactionCategory category = transactionCategoryRepository.getReferenceById(tcPk);

        BigDecimal newPos;
        if (prevCrdtId == null && nextCrdtId == null) {
            // 리스트 비었으면 1, 아니면 맨 뒤
            var tail = planDetailRepository
                    .findTopByTravel_TpPkAndPdDayAndTombstoneFalseOrderByPositionDesc(travelPk, day);
            newPos = tail.map(t -> Positioning.after(t.getPosition()))
                    .orElseGet(() -> new BigDecimal("1"));
        } else if (prevCrdtId == null) {
            PlanDetail next = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelPk, day)
                    .orElseThrow(() -> new IllegalArgumentException("nextCrdtId not found in travel/day"));
            newPos = Positioning.before(next.getPosition());
        } else if (nextCrdtId == null) {
            PlanDetail prev = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelPk, day)
                    .orElseThrow(() -> new IllegalArgumentException("prevCrdtId not found in travel/day"));
            newPos = Positioning.after(prev.getPosition());
        } else {
            PlanDetail prev = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelPk, day)
                    .orElseThrow(() -> new IllegalArgumentException("prevCrdtId not found in travel/day"));
            PlanDetail next = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelPk, day)
                    .orElseThrow(() -> new IllegalArgumentException("nextCrdtId not found in travel/day"));
            newPos = Positioning.mid(prev.getPosition(), next.getPosition());

            // 밀집 방어
            if (newPos.compareTo(prev.getPosition()) <= 0 || newPos.compareTo(next.getPosition()) >= 0) {
                rebalanceRange(travelPk, day);
                prev = planDetailRepository.findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelPk, day).orElseThrow();
                next = planDetailRepository.findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelPk, day).orElseThrow();
                newPos = Positioning.mid(prev.getPosition(), next.getPosition());
            }
        }

        PlanDetail created = PlanDetail.builder()
                .crdtId(UUID.randomUUID() + ":" + clientId)
                .clientId(clientId)
                .opTs(opTs)
                .travel(travel)
                .transactionCategory(category)
                .pdDay(day)
                .position(newPos)
                .pdPlace(pdPlace)
                .pdAddress(pdAddress)
                .pdCost(pdCost)
                .tombstone(false)
                .build();

        return PlanDetailDTO.entityToDTO(planDetailRepository.save(created));
    }

    /* 같은 날 내 이동 */
    public PlanDetailDTO moveWithinDay(String crdtId, String prevCrdtId, String nextCrdtId,
                                       String clientId, Long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId)
                .orElseThrow(() -> new IllegalArgumentException("crdtId not found: " + crdtId));

        Long travelId = target.getTravel().getTpPk();
        int day = target.getPdDay();

        BigDecimal newPos = computeNewPosition(travelId, day, normalize(prevCrdtId), normalize(nextCrdtId));

        target.setPosition(newPos);
        target.setClientId(clientId);
        target.setOpTs(opTs);
        return PlanDetailDTO.entityToDTO(target);
    }

    /* 다른 날짜로 이동 */
    public PlanDetailDTO moveToAnotherDay(String crdtId, int newDay,
                                          String prevCrdtId, String nextCrdtId,
                                          String clientId, Long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId)
                .orElseThrow(() -> new IllegalArgumentException("crdtId not found: " + crdtId));
        Long travelId = target.getTravel().getTpPk();

        BigDecimal newPos = computeNewPosition(travelId, newDay, normalize(prevCrdtId), normalize(nextCrdtId));

        target.setPosition(newPos);
        target.setPdDay(newDay);
        target.setClientId(clientId);
        target.setOpTs(opTs);
        return PlanDetailDTO.entityToDTO(target);
    }

    /* 내용 수정 (선택적 업데이트) */
    public PlanDetailDTO updateFields(String crdtId, String pdPlace, String pdAddress,
                                      Integer pdCost, Long tcPk, String clientId, Long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId)
                .orElseThrow(() -> new IllegalArgumentException("crdtId not found: " + crdtId));

        if (pdPlace != null)   target.setPdPlace(pdPlace);
        if (pdAddress != null) target.setPdAddress(pdAddress);
        if (pdCost != null)    target.setPdCost(pdCost);
        if (tcPk != null)      target.setTransactionCategory(transactionCategoryRepository.getReferenceById(tcPk));

        target.setClientId(clientId);
        target.setOpTs(opTs);
        return PlanDetailDTO.entityToDTO(target);
    }

    /* 소프트 삭제 */
    public void softDelete(String crdtId, String clientId, Long opTs) {
        PlanDetail target = planDetailRepository.findByCrdtId(crdtId)
                .orElseThrow(() -> new IllegalArgumentException("crdtId not found: " + crdtId));
        if (!target.isTombstone()) {
            target.setTombstone(true);
            target.setOpTs(opTs);
            target.setClientId(clientId);
        }
    }

    /* 위치 계산 (여행/날짜 스코프로 prev/next 검색) */
    private BigDecimal computeNewPosition(Long travelId, int day, String prevCrdtId, String nextCrdtId) {
        if (prevCrdtId == null && nextCrdtId == null) {
            var tail = planDetailRepository
                    .findTopByTravel_TpPkAndPdDayAndTombstoneFalseOrderByPositionDesc(travelId, day);
            return tail.map(t -> Positioning.after(t.getPosition()))
                    .orElseGet(() -> new BigDecimal("1"));
        }
        if (prevCrdtId == null) {
            PlanDetail next = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelId, day)
                    .orElseThrow(() -> new IllegalArgumentException("nextCrdtId not found in travel/day"));
            return Positioning.before(next.getPosition());
        }
        if (nextCrdtId == null) {
            PlanDetail prev = planDetailRepository
                    .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelId, day)
                    .orElseThrow(() -> new IllegalArgumentException("prevCrdtId not found in travel/day"));
            return Positioning.after(prev.getPosition());
        }
        PlanDetail prev = planDetailRepository
                .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelId, day)
                .orElseThrow(() -> new IllegalArgumentException("prevCrdtId not found in travel/day"));
        PlanDetail next = planDetailRepository
                .findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelId, day)
                .orElseThrow(() -> new IllegalArgumentException("nextCrdtId not found in travel/day"));

        BigDecimal mid = Positioning.mid(prev.getPosition(), next.getPosition());
        if (mid.compareTo(prev.getPosition()) <= 0 || mid.compareTo(next.getPosition()) >= 0) {
            rebalanceRange(travelId, day);
            prev = planDetailRepository.findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(prevCrdtId, travelId, day).orElseThrow();
            next = planDetailRepository.findByCrdtIdAndTravel_TpPkAndPdDayAndTombstoneFalse(nextCrdtId, travelId, day).orElseThrow();
            mid = Positioning.mid(prev.getPosition(), next.getPosition());
        }
        return mid;
    }

    /* 리밸런스: 해당 day만 등차수열로 재부여 */
    public void rebalanceRange(Long travelId, int day) {
        List<PlanDetail> list = planDetailRepository.findAliveByTravelAndDayOrderByPosition(travelId, day);
        BigDecimal step = new BigDecimal("10");
        BigDecimal cur = step;

        for (PlanDetail p : list) {
            p.setPosition(cur);
            cur = cur.add(step);
        }
    }

    private static String normalize(String raw) {
        if (raw == null) return null;
        String t = raw.trim();
        if (t.isEmpty()) return null;
        if ("0".equals(t)) return null;
        if ("null".equalsIgnoreCase(t)) return null;
        return t;
    }
}
