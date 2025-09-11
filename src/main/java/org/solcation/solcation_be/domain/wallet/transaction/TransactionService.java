package org.solcation.solcation_be.domain.wallet.transaction;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionCategoryDTO;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionDTO;
import org.solcation.solcation_be.domain.wallet.transaction.dto.TransactionDetailDTO;
import org.solcation.solcation_be.domain.wallet.transaction.dto.UpdateMemoReqDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.repository.*;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.solcation.solcation_be.util.category.TransactionCategoryLookup;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final SharedAccountRepository sharedAccountRepository;
    private final CardRepository cardRepository;
    private final JPAQueryFactory queryFactory;
    private final TransactionCategoryLookup  transactionCategoryLookup;


    /* 전체 거래 내역 렌더링(필터링 포함 - 거래 유형으로 필터링) */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsAll(Long groupPk, JwtPrincipal principal, TRANSACTIONTYPE tType) {
        BooleanBuilder builder = new BooleanBuilder();
        QTransaction t = QTransaction.transaction;

        //유저 조회, 해당 그룹의 sa 조회
        Group group = groupRepository.findByGroupPk(groupPk);
        SharedAccount sa = sharedAccountRepository.findByGroup(group).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ACCOUNT));

        //sa_pk가 일치
        builder.and(t.saPk.eq(sa));

        // null이 아니면 필터링, null이면 전체
        if(tType != null) {
            builder.and(t.transactionType.eq(tType));
        }

        List<Transaction> list = queryFactory
                                    .selectFrom(t)
                                    .where(builder)
                                    .orderBy(t.satTime.desc())
                                    .fetch();

        List<TransactionDTO> result = new ArrayList<>();
        list.forEach(i -> result.add(
                TransactionDTO.builder()
                        .satPk(i.getSatPk())
                        .satTime(i.getSatTime())
                        .briefs(i.getBriefs())
                        .tcName(i.getTcPk().getTcName())
                        .tType(i.getTransactionType().name())
                        .satAmount(i.getSatAmount())
                        .balance(i.getBalance())
                        .build()
        ));

        return result;
    }

    /* 카드 거래 내역 렌더링(필터링 포함 - 월별로 필터링) */
    @Transactional(readOnly = true)
    public List<TransactionDTO> getTransactionsCard(Long groupPk, JwtPrincipal principal, YearMonth ym) {
        BooleanBuilder builder = new BooleanBuilder();
        QTransaction t = QTransaction.transaction;

        //유저 조회, 해당 그룹의 sa 조회, 해당 유저 카드 조회
        Group group = groupRepository.findByGroupPk(groupPk);
        User user = userRepository.findByUserId(principal.userId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        SharedAccount sa = sharedAccountRepository.findByGroup(group).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ACCOUNT));
        Card card = cardRepository.findBySaPk_GroupAndGmPk_UserAndCancellationFalse(group, user).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CARD));

        //sa_pk가 일치, sac_pk가 일치
        builder.and(t.saPk.eq(sa)).and(t.sacPk.eq(card));

        //카드 거래만
        builder.and(t.transactionType.eq(TRANSACTIONTYPE.CARD));

        //년/월 필터링
        ZonedTimeRange r = ZonedTimeUtil.month(ym);
        builder.and(t.satTime.between(r.start(), r.end()));

        List<Transaction> list = queryFactory
                .selectFrom(t)
                .where(builder)
                .orderBy(t.satTime.desc())
                .fetch();

        List<TransactionDTO> result = new ArrayList<>();
        list.forEach(i -> result.add(
                TransactionDTO.builder()
                        .satPk(i.getSatPk())
                        .satTime(i.getSatTime())
                        .briefs(i.getBriefs())
                        .tcName(i.getTcPk().getTcName())
                        .tType(i.getTransactionType().name())
                        .satAmount(i.getSatAmount())
                        .balance(i.getBalance())
                        .build()
        ));

        return result;
    }

    /* 이용 내역 상세 렌더링 */
    public TransactionDetailDTO getTransactionDetail(Long satPk) {
        QTransaction t = QTransaction.transaction;
        QTransactionCategory tc = QTransactionCategory.transactionCategory;
        QUser u = QUser.user;

        return queryFactory.select(Projections.constructor(
                TransactionDetailDTO.class,
                t.satPk,
                t.tcPk,
                t.satTime,
                t.briefs,
                t.transactionType,
                t.userPk.userName,
                t.depositDestination,
                t.withdrawDestination,
                t.balance,
                t.satAmount,
                t.satMemo
        ))
                .from(t)
                .leftJoin(t.tcPk, tc)
                .leftJoin(t.userPk, u)
                .where(t.satPk.eq(satPk))
                .fetchOne();
    }

    /* 카테고리 목록 렌더링 */
    public List<TransactionCategoryDTO> getTransactionCategories() {
        List<TransactionCategory> list = transactionCategoryLookup.getList();
        List<TransactionCategoryDTO> result = new ArrayList<>();
        list.forEach(i -> result.add(TransactionCategoryDTO.builder()
                .tcPk(i.getTcPk())
                .tcName(i.getTcName())
                .tcIcon(i.getTcIcon())
                .tcCode(i.getTcCode())
                .build()));
        return result;
    }

    /* 지출 카테고리 변경 */
    public void updateTransactionCategory() {

    }

    /* 메모 수정 */
    public void updateMemo(UpdateMemoReqDTO dto) {
        Transaction t = transactionRepository.findBySatPk(dto.getSatPk());
        t.updateMemo(dto.getMemo());
        transactionRepository.save(t);
    }
}
