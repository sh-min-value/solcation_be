package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.PlanDetail;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDetailRepository extends JpaRepository<PlanDetail, Long> {

    // 같은 여행ID, tombstone=false 정렬 조회
    @Query("select p from PlanDetail p " +
            "where p.travel.tpPk = :travelId and p.tombstone = false " +
            "order by p.position asc, p.opTs asc, p.clientId asc, p.crdtId asc")
    List<PlanDetail> findAliveByTravelOrderByPosition(
            @Param("travelId") Long travelId);

    // 같은 여행ID, 같은 Day, tombstone=false 만 정렬 조회
    @Query("select p from PlanDetail p " +
            "where p.travel.tpPk = :travelId and p.pdDay = :day and p.tombstone = false " +
            "order by p.position asc, p.opTs asc, p.clientId asc, p.crdtId asc")
    List<PlanDetail> findAliveByTravelAndDayOrderByPosition(
            @Param("travelId") Long travelId, @Param("day") Integer day);

    Optional<PlanDetail> findByCrdtId(String crdtId);

    // prev/next를 찾을 때 사용 (동일 day, 동일 travel)
    @Query("select p from PlanDetail p " +
            "where p.travel.tpPk = :travelId and p.pdDay = :day and p.tombstone = false " +
            "and p.position < :pos order by p.position desc")
    List<PlanDetail> findPrev(@Param("travelId") Long travelId, @Param("day") int day, @Param("pos") BigDecimal pos, Pageable pageable);

    @Query("select p from PlanDetail p " +
            "where p.travel.tpPk = :travelId and p.pdDay = :day and p.tombstone = false " +
            "and p.position > :pos order by p.position asc")
    List<PlanDetail> findNext(@Param("travelId") Long travelId, @Param("day") int day, @Param("pos") BigDecimal pos, Pageable pageable);

}