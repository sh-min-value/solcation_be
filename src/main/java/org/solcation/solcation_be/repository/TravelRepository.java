package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
import org.solcation.solcation_be.domain.stats.dto.FinishTravelListDTO;
import org.solcation.solcation_be.entity.PlanDetail;
import org.solcation.solcation_be.entity.Travel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    // 그룹 전체 여행 (정렬)
    List<Travel> findAllByGroup_GroupPkOrderByTpStartDesc(Long groupPk);

    // 그룹 + 상태별 여행 (정렬)
    List<Travel> findAllByGroup_GroupPkAndTpStateOrderByTpStartDesc(Long groupPk, TRAVELSTATE state);

    // 메인페이지 현재 월별 일정 조회
    List<Travel> findAllByGroup_GroupPkInAndTpStartLessThanEqualAndTpEndGreaterThanEqualOrderByTpStartAsc(List<Long> groupPks, LocalDate monthEnd, LocalDate monthStart);

    // 통계 페이지 완료한 여행 렌더링
    List<Travel> findByGroup_GroupPkAndTpStateOrderByTpEndDesc(Long groupPk, TRAVELSTATE tpState);

    // 우리 그룹과 같은 여행지, 우리 그룹을 제외한 다른 그룹들의 여행
    List<Travel> findByTpLocationAndGroup_GroupPkNotAndTpState(String tpLocation, Long excludeGroupPk, TRAVELSTATE state);

    //해당 그룹 여행인지 확인
    boolean existsByGroup_GroupPkAndTpPk(Long groupPk, Long tpPk);

    //여행 삭제
    void deleteTravelByTpPk(Long tpPk);
}
