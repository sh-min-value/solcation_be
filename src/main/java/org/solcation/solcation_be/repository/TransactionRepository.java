package org.solcation.solcation_be.repository;

import org.solcation.solcation_be.domain.stats.dto.CategorySpentDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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
                             and (t.transactionType is null or t.transactionType <> :excludedType)
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
                                         @Param("endExclusive") Instant endExclusive,
                                         @Param("excludedType") TRANSACTIONTYPE excludedType);

    // 특정 여행의 1일, 1인당 평균 소비량


    // 특정 여행지에서 본인 그룹을 제외한 나머지 그룹들의 1일, 1인당 평균



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
}
