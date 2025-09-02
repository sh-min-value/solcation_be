package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.TRAVELSTATE;
import org.solcation.solcation_be.entity.Travel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TravelRepository extends JpaRepository<Travel, Long> {
    // 그룹 전체 여행 (정렬)
    List<Travel> findAllByGroup_GroupPkOrderByTpStartDesc(Long groupPk);

    // 그룹 + 상태별 여행 (정렬)
    List<Travel> findAllByGroup_GroupPkAndTpStateOrderByTpStartDesc(Long groupPk, TRAVELSTATE state);
}
