package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>, QuerydslPredicateExecutor<Transaction> {

    // 해당 여행 총 사용액 합계
    @Query("""
            select coalesce(sum(t.satAmount), 0)
            from Transaction t
              join t.gmPk gm
              join gm.group g
              join g.travels tp
            where tp.tpPk = :tpPk
              and t.transactionType in :types
              and t.satTime >= :start
              and t.satTime < :endExclusive
            """)
    long sumSpentTravel(@Param("tpPk") Long tpPk,
                        @Param("types") List<TRANSACTIONTYPE> types,
                        @Param("start") Instant start,
                        @Param("endExclusive") Instant endExclusive);

    // 삭제 되지 않은 여행 계획의 예산 합계
    @Query("""
            select coalesce(sum(p.pdCost), 0)
            from PlanDetail p
            where p.travel.tpPk = :tpPk
              and p.tombstone = false
            """)
    long sumPlannedTravel(@Param("tpPk") Long tpPk);

    // 소비 카테고리 별 실제 소비량
    @Query("""
            select new org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO(
                tc.tcPk,
                tc.tcName,
                tc.tcCode,
                coalesce(sum(
                    case
                        when tp.tpPk = :tpPk
                             and t.satTime >= :start
                             and t.satTime < :endExclusive
                             and t.tcPk is not null
                             and (t.transactionType is null or t.transactionType <> org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.DEPOSIT)
                        then t.satAmount
                        else 0
                    end
                ), 0L)
            )
            from TransactionCategory tc
            left join Transaction t on t.tcPk = tc
            left join t.gmPk gm
            left join gm.group g
            left join g.travels tp
            group by tc.tcPk, tc.tcName, tc.tcCode
            order by tc.tcPk
            """)
    List<CategorySpentDTO> categorySpent(@Param("tpPk") Long tpPk,
                                         @Param("start") Instant start,
                                         @Param("endExclusive") Instant endExclusive);

    // 같은 여행지를 여행한 다른 그룹의 소비 총합
    @Query(value = """
             SELECT COALESCE(SUM(t2.sat_amount), 0)
             FROM travel_plan_tb tr
             JOIN travel_plan_tb tr2
               ON tr2.tp_location = tr.tp_location
              AND tr2.group_pk <> tr.group_pk
             JOIN shared_account_tb s2
               ON s2.group_pk = tr2.group_pk
             JOIN shared_account_transaction_tb t2
               ON t2.sa_pk = s2.sa_pk
             WHERE tr.tp_pk = :tpPk
               AND tr2.tp_state = 2
               AND t2.transaction_type IN (:types)
               AND t2.sat_time >= CONVERT_TZ(CONCAT(tr2.tp_start,' 00:00:00'),'Asia/Seoul','UTC')
               AND t2.sat_time <  CONVERT_TZ(DATE_ADD(CONCAT(tr2.tp_end,' 00:00:00'), INTERVAL 1 DAY),'Asia/Seoul','UTC')
            """, nativeQuery = true)
    long sumOthersSpentBySameLocation(@Param("tpPk") Long tpPk,
                                      @Param("types") List<TRANSACTIONTYPE> types);

    // 다른 그룹의 참여자 x 여행일수
    @Query(value = """
             SELECT COALESCE(SUM(tr2.participant * (DATEDIFF(tr2.tp_end, tr2.tp_start) + 1)), 0)
             FROM travel_plan_tb tr
             JOIN travel_plan_tb tr2
               ON tr2.tp_location = tr.tp_location
              AND tr2.group_pk <> tr.group_pk
            WHERE tr.tp_pk = :tpPk
              AND tr2.tp_state = 2
            """, nativeQuery = true)
    long sumOthersPersonDays(@Param("tpPk") Long tpPk);

    // 다른 그룹의 카테고리별 합계 평균
    @Query(value = """
            SELECT 
                tc.tc_pk AS tcPk,
                tc.tc_name AS tcName,
                tc.tc_code AS tcCode,
                COALESCE(ROUND(AVG(ts.amount_sum)), 0) AS totalAmount
            FROM transaction_category_tb tc
            LEFT JOIN (
                SELECT 
                    t2.tc_pk AS tc_pk,
                    tr2.tp_pk AS tp_pk,
                    COALESCE(SUM(
                        CASE 
                            WHEN (t2.transaction_type IS NULL OR t2.transaction_type <> 0)
                            THEN t2.sat_amount
                            ELSE 0
                        END
                    ), 0) AS amount_sum
                FROM travel_plan_tb tr
                JOIN travel_plan_tb tr2
                     ON tr2.tp_location = tr.tp_location
                    AND tr2.tp_pk <> tr.tp_pk
                JOIN shared_account_tb s2
                     ON s2.group_pk = tr2.group_pk
                JOIN shared_account_transaction_tb t2
                     ON t2.sa_pk = s2.sa_pk
                WHERE tr.tp_pk = :tpPk
                  AND t2.tc_pk IS NOT NULL
                  AND t2.sat_time >= CONVERT_TZ(CONCAT(tr2.tp_start,' 00:00:00'),'Asia/Seoul','UTC')
                  AND t2.sat_time <  CONVERT_TZ(DATE_ADD(CONCAT(tr2.tp_end,' 00:00:00'), INTERVAL 1 DAY),'Asia/Seoul','UTC')
                GROUP BY t2.tc_pk, tr2.tp_pk
            ) ts
              ON ts.tc_pk = tc.tc_pk
            GROUP BY tc.tc_pk, tc.tc_name, tc.tc_code
            ORDER BY tc.tc_pk
            """, nativeQuery = true)
    List<Object[]> categoryOthersAvgPerTravel(@Param("tpPk") Long tpPk);

    // 계획 상 카테고리별 소비 통게
    @Query("""
            select new org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO(
                tc.tcPk,
                tc.tcName,
                tc.tcCode,
                coalesce(sum(
                    case
                        when p.travel.tpPk = :tpPk
                             and p.tombstone = false
                             and p.transactionCategory is not null
                        then p.pdCost
                        else 0
                    end
                ), 0L)
            )
            from TransactionCategory tc
            left join PlanDetail p on p.transactionCategory = tc
            group by tc.tcPk, tc.tcName, tc.tcCode
            order by tc.tcPk
            """)
    List<CategorySpentDTO> plannedCategorySpentOfTravel(@Param("tpPk") Long tpPk);

    //기간 동안의 총 거래액 추출
    @Query("""
            SELECT COALESCE(SUM(t.satAmount) , 0)
            FROM Transaction t
            WHERE t.transactionType = :transactionType AND t.sacPk = :sacPk AND t.satTime >= :from AND t.satTime < :to
            """)
    Long findTotalAmountForPeriod(@Param("transactionType") TRANSACTIONTYPE transactionType,
                                  @Param("sacPk") Card sacPk,
                                  @Param("from") Instant from, @Param("to") Instant to);

    //pk로 조회
    Transaction findBySatPk(@Param("satPk") Long satPk);

    //pk + group으로 조회
    boolean existsBySatPkAndGmPk_Group_GroupPk(@Param("satPk") Long satPk, @Param("groupPk") Long groupPk);

    // 그룹의 총 여행 횟수
    @Query("""
            select count(tr)
            from Travel tr
            where tr.group.groupPk = :groupPk
                        and tr.tpState = org.solcation.solcation_be.entity.enums.TRAVELSTATE.FINISH
            """)
    long countTripsByGroup(@Param("groupPk") Long groupPk);

    // 그룹의 총 여행 일수 합계
    @Query("""
            select coalesce(
                sum( cast(function('datediff', tr.tpEnd, tr.tpStart) as long) + 1L ),
                0L
            )
            from Travel tr
            where tr.group.groupPk = :groupPk
              and tr.tpState = org.solcation.solcation_be.entity.enums.TRAVELSTATE.FINISH
            """)
    long sumTripDaysByGroup(@Param("groupPk") Long groupPk);

    // 그룹의 여행 기간 동안 실제 지출 합계
    @Query("""
            select coalesce(sum(
                case
                    when t.transactionType in (
                         org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.WITHDRAW,
                         org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.CARD
                    )
                    and exists (
                        select 1 from Travel tr
                        where tr.group.groupPk = :groupPk
                          and function('date', function('convert_tz', t.satTime, '+00:00', '+09:00'))
                              between tr.tpStart and tr.tpEnd
                    )
                    then t.satAmount
                    else 0
                end
            ), 0)
            from Transaction t
            where t.saPk.group.groupPk = :groupPk
            """)
    long sumSpentOnTravelDays(@Param("groupPk") Long groupPk);

    // 그룹의 여행 기간 동안 카테고리별 합계
    @Query("""
            select tc.tcPk,
                   tc.tcName,
                   tc.tcCode,
                   coalesce(sum(
                       case
                           when t.transactionType in (
                                org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.WITHDRAW,
                                org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.CARD
                           )
                           and g.groupPk = :groupPk
                           and exists (
                               select 1
                               from Travel tr
                               where tr.group = g
                                 and function('date', function('convert_tz', t.satTime, '+00:00', '+09:00'))
                                     between tr.tpStart and tr.tpEnd
                           )
                           then t.satAmount
                           else 0
                       end
                   ), 0)
            from TransactionCategory tc
            left join Transaction t on t.tcPk = tc
            left join t.gmPk gm
            left join gm.group g
            group by tc.tcPk, tc.tcName, tc.tcCode
            order by tc.tcPk
            """)
    List<Object[]> categoryAmountsOnTravelDays(@Param("groupPk") Long groupPk);
}
