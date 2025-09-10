package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // 해당 여행 총 사용액 합계
    @Query("""
            select coalesce(sum(t.satAmount), 0)
            from Transaction t
              join t.gmPk gm
              join gm.group g
              join g.travels tp
            where tp.tpPk = :tpPk
              and t.transactionType in :types
              and t.satTime between :start and :end
            """)
    long sumSpentTravel(@Param("tpPk") Long tpPk, @Param("types") List<TRANSACTIONTYPE> types, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

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
                            when t.satTime between :start and :end
                                 and t.tcPk is not null
                                 and (t.transactionType is null
                                      or t.transactionType <> org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE.DEPOSIT)
                            then t.satAmount
                            else 0
                        end
                    ), 0L)
                )
                from TransactionCategory tc
                left join Transaction t on t.tcPk = tc
                group by tc.tcPk, tc.tcName, tc.tcCode
                order by tc.tcPk
            """)
    List<CategorySpentDTO> categorySpent(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 같은 여행지를 여행한 다른 그룹과의 1일, 1인당 소비 비교
    @Query(value = """
        SELECT
          CAST(FLOOR(
              (
                SELECT COALESCE(SUM(t.sat_amount), 0)
                FROM shared_account_transaction_tb t
                JOIN shared_account_tb s ON t.sa_pk = s.sa_pk
                WHERE s.group_pk = tr.group_pk
                  AND t.sat_time >= tr.tp_start
                  AND t.sat_time < DATE_ADD(tr.tp_end, INTERVAL 1 DAY)
                  AND t.transaction_type IN (1,2)
              ) / NULLIF((tr.participant * (DATEDIFF(tr.tp_end, tr.tp_start) + 1)), 0)
          ) AS SIGNED) AS ourPerPersonPerDay,

          COALESCE( CAST(FLOOR(
              (
                SELECT COALESCE(SUM(t2.sat_amount), 0)
                FROM travel_plan_tb tr2
                JOIN shared_account_tb s2 ON s2.group_pk = tr2.group_pk
                JOIN shared_account_transaction_tb t2 ON t2.sa_pk = s2.sa_pk
                WHERE tr2.tp_location = tr.tp_location
                  AND tr2.group_pk <> tr.group_pk
                  AND t2.sat_time >= tr2.tp_start
                  AND t2.sat_time < DATE_ADD(tr2.tp_end, INTERVAL 1 DAY)
                  AND t2.transaction_type IN (1,2)
              ) / NULLIF((
                    SELECT COALESCE(SUM(tr2.participant * (DATEDIFF(tr2.tp_end, tr2.tp_start) + 1)), 0)
                    FROM travel_plan_tb tr2
                    WHERE tr2.tp_location = tr.tp_location
                      AND tr2.group_pk <> tr.group_pk
              ), 0)
          ) AS SIGNED), 0) AS othersPerPersonPerDay,

          CAST(
              CAST(FLOOR(
                  (
                    SELECT COALESCE(SUM(t.sat_amount), 0)
                    FROM shared_account_transaction_tb t
                    JOIN shared_account_tb s ON t.sa_pk = s.sa_pk
                    WHERE s.group_pk = tr.group_pk
                      AND t.sat_time >= tr.tp_start
                      AND t.sat_time < DATE_ADD(tr.tp_end, INTERVAL 1 DAY)
                      AND t.transaction_type IN (1,2)
                  ) / NULLIF((tr.participant * (DATEDIFF(tr.tp_end, tr.tp_start) + 1)), 0)
              ) AS SIGNED)
              -
              COALESCE(CAST(FLOOR(
                  (
                    SELECT COALESCE(SUM(t2.sat_amount), 0)
                    FROM travel_plan_tb tr2
                    JOIN shared_account_tb s2 ON s2.group_pk = tr2.group_pk
                    JOIN shared_account_transaction_tb t2 ON t2.sa_pk = s2.sa_pk
                    WHERE tr2.tp_location = tr.tp_location
                      AND tr2.group_pk <> tr.group_pk
                      AND t2.sat_time >= tr2.tp_start
                      AND t2.sat_time < DATE_ADD(tr2.tp_end, INTERVAL 1 DAY)
                      AND t2.transaction_type IN (1,2)
                  ) / NULLIF((
                        SELECT COALESCE(SUM(tr2.participant * (DATEDIFF(tr2.tp_end, tr2.tp_start) + 1)), 0)
                        FROM travel_plan_tb tr2
                        WHERE tr2.tp_location = tr.tp_location
                          AND tr2.group_pk <> tr.group_pk
                  ), 0)
              ) AS SIGNED), 0)
          AS SIGNED) AS diffAbs
        FROM travel_plan_tb tr
        WHERE tr.tp_pk = :tpPk
        """, nativeQuery = true)
    Object[] compareTravelSpend(@Param("tpPk") Long tpPk);

    @Query("""
    SELECT COALESCE(SUM(t.satAmount) , 0)
    FROM Transaction t
    WHERE t.saPk = :saPk AND t.transactionType = :transactionType AND t.userPk = :userPk AND t.sacPk = :sacPk AND t.satTime >= :start AND t.satTime < :end
    """)
    Long findTotalAmountForPeriod(@Param("saPk") SharedAccount saPk,
                                                   @Param("transactionType") TRANSACTIONTYPE transactionType,
                                                   @Param("userPk") User user,
                                                   @Param("sacPk") Card sacPk,
                                                   Instant from, Instant to);
}
