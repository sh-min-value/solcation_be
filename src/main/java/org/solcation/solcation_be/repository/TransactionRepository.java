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
                when t.satTime >= :start
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
    group by tc.tcPk, tc.tcName, tc.tcCode
    order by tc.tcPk
    """)
    List<CategorySpentDTO> categorySpent(@Param("start") Instant start,
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
            """, nativeQuery = true)
    long sumOthersPersonDays(@Param("tpPk") Long tpPk);

    //기간 동안의 총 거래액 추출
    @Query("""
            SELECT COALESCE(SUM(t.satAmount) , 0)
            FROM Transaction t
            WHERE t.saPk = :saPk AND t.transactionType = :transactionType AND t.userPk = :userPk AND t.sacPk = :sacPk AND t.satTime >= :from AND t.satTime < :to
            """)
    Long findTotalAmountForPeriod(@Param("saPk") SharedAccount saPk,
                                  @Param("transactionType") TRANSACTIONTYPE transactionType,
                                  @Param("userPk") User user,
                                  @Param("sacPk") Card sacPk,
                                  @Param("from") Instant from, @Param("to") Instant to);

    //pk로 조회
    Transaction findBySatPk(@Param("satPk") Long satPk);
}
