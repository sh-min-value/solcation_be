package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.enums.TRAVELSTATE;
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
}
