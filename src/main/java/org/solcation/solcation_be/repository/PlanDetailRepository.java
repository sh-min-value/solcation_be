package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.entity.PlanDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlanDetailRepository extends JpaRepository<PlanDetail, Long> {

    // 같은 여행ID, tombstone=false 정렬 조회
    @Query("""
        select p from PlanDetail p
        where p.travel.tpPk = :travelId and p.tombstone = false
        order by p.pdDay asc, p.position asc, p.opTs asc, p.clientId asc, p.crdtId asc
    """)
    List<PlanDetail> findAliveByTravelOrderByPdDayAscPositionAsc(@Param("travelId") Long travelId);

    // 같은 여행ID, 같은 Day, tombstone=false 만 정렬 조회
    @Query("""
        select p from PlanDetail p
        where p.travel.tpPk = :travelId and p.pdDay = :day and p.tombstone = false
        order by p.position asc, p.opTs asc, p.clientId asc, p.crdtId asc
    """)
    List<PlanDetail> findAliveByTravelAndDayByPdDayAscPositionAsc(
            @Param("travelId") Long travelId, @Param("day") Integer day);

    @Query("SELECT DISTINCT p.pdDay FROM PlanDetail p WHERE p.travel.tpPk = :travelId AND p.tombstone = false ORDER BY p.pdDay ASC")
    List<Integer> findTravelDays(long travelId);

    Optional<PlanDetail> findByCrdtId(String crdtId);

    void deleteAllByTravel_TpPk(Long travelId);
}