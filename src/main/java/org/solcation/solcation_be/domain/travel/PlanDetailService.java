package org.solcation.solcation_be.domain.travel;

import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;

import org.solcation.solcation_be.domain.travel.dto.PlanDetailDTO;
import org.solcation.solcation_be.domain.travel.util.Positioning;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.solcation.solcation_be.repository.PlanDetailRepository;
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

    // 여행 세부계획 조회
    @Transactional(readOnly = true)
    public List<PlanDetailDTO> getTravelPlans(Long travelPk) {
        List<PlanDetail> list = planDetailRepository.findAliveByTravelOrderByPosition(travelPk);
        return list.stream()
                .map(PlanDetailDTO::entityToDTO)
                .toList();
    }

    // 여행 세부계획 삽입
    public PlanDetailDTO insertBetween(Long travelPk, int day,
                                    String prevCrdtId, String nextCrdtId,
                                    String pdPlace, String pdAddress, int pdCost,
                                    String clientId, long opTs) {
        Travel travel = travelRepository.getReferenceById(travelPk);

        BigDecimal newPos;
        if(prevCrdtId == null && nextCrdtId == null) {
            newPos = new BigDecimal("1");
        } else if (prevCrdtId == null) {
            PlanDetail next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
            newPos = Positioning.before(next.getPosition());

        } else if (nextCrdtId == null) {
            PlanDetail prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
            newPos = Positioning.after(prev.getPosition());
        } else {
            PlanDetail prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
            PlanDetail next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
            newPos = Positioning.mid(prev.getPosition(), next.getPosition());

            // id rebalance
            if(newPos.compareTo(prev.getPosition()) == 0 ||
                    newPos.compareTo(next.getPosition()) == 0) {
                rebalanceRange(travelPk, day);
                prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
                next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
                newPos = Positioning.mid(prev.getPosition(), next.getPosition());
            }
        }
        PlanDetail created = PlanDetail.builder()
                .crdtId(UUID.randomUUID()+":"+clientId)
                .opTs(opTs)
                .travel(travel)
                .pdDay(day)
                .position(newPos)
                .pdPlace(pdPlace)
                .pdAddress(pdAddress)
                .pdCost(pdCost)
                .tombstone(false)
                .build();

        return PlanDetailDTO.entityToDTO(planDetailRepository.save(created));
    }

    //당일 내 이동
    public PlanDetailDTO moveWithinDay(
            String crdtId, String prevCrdtId, String nextCrdtId,
            String clientId, long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId).orElseThrow();
        Long travelId = target.getTravel().getTpPk();
        int day = target.getPdDay();

        BigDecimal newPos = computeNewPosition(travelId, day, prevCrdtId, nextCrdtId);

        target.setPosition(newPos);
        target.setClientId(clientId);
        target.setOpTs(opTs);
        return PlanDetailDTO.entityToDTO(target);
    }

    //다른 날짜로 이동
    public PlanDetailDTO moveToAnotherDay(
            String crdtId, int newDay, String prevCrdtId, String nextCrdtId,
            String clientId, long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId).orElseThrow();
        Long travelId = target.getTravel().getTpPk();

        BigDecimal newPos = computeNewPosition(travelId, newDay, prevCrdtId, nextCrdtId);

        target.setPosition(newPos);
        target.setClientId(clientId);
        target.setOpTs(opTs);
        target.setPdDay(newDay);

        return PlanDetailDTO.entityToDTO(target);
    }

    //세부계획 카드 수정
    public PlanDetailDTO updateFields( String crdtId, String pdPlace, String pdAddress,
                                    Integer pdCost, String clientId, long opTs) {

        PlanDetail target = planDetailRepository.findByCrdtId(crdtId).orElseThrow();

        target.setPdPlace(pdPlace);
        target.setPdAddress(pdAddress);
        target.setPdCost(pdCost);
        target.setClientId(clientId);
        target.setOpTs(opTs);

        return PlanDetailDTO.entityToDTO(target);
    }

    //삭제
    public void softDelete(String crdtId, String clientId, long opTs) {
        PlanDetail target = planDetailRepository.findByCrdtId(crdtId).orElseThrow();
        if (!target.isTombstone()) {
            target.setTombstone(true);
            target.setOpTs(opTs);
            target.setClientId(clientId);
        }
    }

    //이동 위치 계산
    private BigDecimal computeNewPosition(Long travelId, int day, String prevCrdtId, String nextCrdtId) {
        if (prevCrdtId == null && nextCrdtId == null) return new BigDecimal("1");
        if (prevCrdtId == null) {
            PlanDetail next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
            return Positioning.before(next.getPosition());
        }
        if (nextCrdtId == null) {
            PlanDetail prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
            return Positioning.after(prev.getPosition());
        }
        PlanDetail prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
        PlanDetail next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
        BigDecimal mid = Positioning.mid(prev.getPosition(), next.getPosition());
        if (mid.compareTo(prev.getPosition()) == 0 || mid.compareTo(next.getPosition()) == 0) {
            rebalanceRange(travelId, day);
            prev = planDetailRepository.findByCrdtId(prevCrdtId).orElseThrow();
            next = planDetailRepository.findByCrdtId(nextCrdtId).orElseThrow();
            mid = Positioning.mid(prev.getPosition(), next.getPosition());
        }
        return mid;
    }

    //id가 촘촘할때 재배치
    public void rebalanceRange(Long travelId, int day) {
        List<PlanDetail> list = planDetailRepository.findAliveByTravelAndDayOrderByPosition(travelId, day);
        BigDecimal step = new BigDecimal("10");
        BigDecimal cur = step;

        for (PlanDetail p : list) {
            p.setPosition(step.add(p.getPosition()));
            cur = cur.add(step);
        }
    }



}
