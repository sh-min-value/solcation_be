package org.solcation.solcation_be.domain.wallet.card;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.domain.wallet.card.dto.CardInfoDTO;
import org.solcation.solcation_be.domain.wallet.card.dto.OpenCardReqDTO;
import org.solcation.solcation_be.entity.*;
import org.solcation.solcation_be.entity.enums.TRANSACTIONCODE;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.repository.*;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.solcation.solcation_be.util.category.TransactionCategoryLookup;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CardService {
    private static final String BIN = "202504"; // SOLCATION 고유번호 6자리
    private static final SecureRandom random = new SecureRandom();

    private final CardRepository cardRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SharedAccountRepository sharedAccountRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionCategoryLookup transactionCategoryLookup;

    /* 카드 개설 */
    @Transactional
    public void openCard(Long groupPk, JwtPrincipal principal, OpenCardReqDTO dto) {
        Group group = groupRepository.findByGroupPk(groupPk);
        User user = userRepository.findByUserId(principal.userId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupMember gm = groupMemberRepository.findByUserAndGroup(user, group);

        //그룹에 모임통장이 존재하지 않는 경우
        SharedAccount sa = sharedAccountRepository.findByGroup(group).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ACCOUNT));


        //해당 유저가 그룹에 이미 개설한 카드가 있는지 확인
        boolean exists = cardRepository.existsBySaPk_GroupAndGmPk_UserAndCancellationFalse(group, user);

        if(exists) {
            throw new CustomException(ErrorCode.CARD_ALREADY_EXISTS);
        }

        //카드 번호 생성(중복 X)
        List<String> cardNums = cardRepository.findAllSacNums();
        String num = generateCardNum();
        while(cardNums.contains(num)) {
            num = generateCardNum();
        }

        //cvc 생성
        String cvc = generateCvc();

        //유효기간 생성
        YearMonth exp = YearMonth.now().plusYears(5);

        short expirationYear = (short) exp.getYear();
        byte expirationMonth = (byte) exp.getMonthValue();

        Card card = Card.builder()
                .sacNum(num)
                .saPk(sa)
                .createdAt(Instant.now())
                .cvc(cvc)
                .pw(dto.getPw())
                .cancellation(false)
                .gmPk(gm)
                .expirationMonth(expirationMonth)
                .expirationYear(expirationYear)
                .build();

        cardRepository.save(card);
    }

    /* 카드 정보 렌더링 */
    @Transactional
    public CardInfoDTO getCardInfo(Long groupPk, JwtPrincipal principal) {
        Group group = groupRepository.findByGroupPk(groupPk);
        User user = userRepository.findByUserId(principal.userId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        SharedAccount sa = sharedAccountRepository.findByGroup(group).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ACCOUNT));
        //카드 번호 조회
        Card card = cardRepository.findBySaPk_GroupAndGmPk_UserAndCancellationFalse(group, user).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CARD));

        //이번 달 카드 이용 총 금액 조회 (sa_pk, transaction_type, user_pk, tc_pk, sac_pk, gm_pk, 이번달) -> sat_amount
        YearMonth nowYm = YearMonth.now(ZoneOffset.UTC);
        Instant from = nowYm.atDay(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant to = nowYm.plusMonths(1).atDay(1)
                .atStartOfDay(ZoneOffset.UTC).toInstant();

        log.info("From: {} / to: {}", from, to);
        Long total = transactionRepository.findTotalAmountForPeriod(sa, TRANSACTIONTYPE.CARD, user, card, from, to);

        CardInfoDTO result = CardInfoDTO.builder()
                .cardNum(card.getSacNum())
                .totalCost(total)
                .build();

        return result;
    }

    /* 카드 거래 내역 렌더링(필터링 포함) */
    @Transactional
    public void getCardTransactionsByFiltering() {

    }

    /* 카드 해지 */
    @Transactional
    public void cancelCard(Long groupPk, JwtPrincipal principal) {
        //groupPk, userPk로 카드 조회
        Group group = groupRepository.findByGroupPk(groupPk);
        User user = userRepository.findByUserId(principal.userId()).orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        Card card = cardRepository.findBySaPk_GroupAndGmPk_UserAndCancellationFalse(group, user).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_CARD));

        //cancellation(1), cancellation_date(now) 수정
        card.updateCancellation();

        cardRepository.save(card);
    }

    /** 카드 번호 생성(Luhn 알고리즘)
     * 1-4: 카드 브랜드(네트워크) 고유 번호
     * 5-6: 카드를 발급한 금융 기관 코드
     * 7-15: 각 카드 발급사의 규칙에 따른 번호
     * 16: 검증용
     */
    public String generateCardNum() {
        String partial = BIN + createMiddleCardNum();
        String checkSum = createCheckSum(partial);

        return partial +  checkSum;
    }

    /* 중간 9자리 생성 */
    public String createMiddleCardNum() {
        return String.format("%09d", random.nextLong(1_000_000_000L));
    }

    /* checksum */
    public String createCheckSum(String partial) {
        int sum = 0;
        boolean doubleCheck = true;

        for(int i = partial.length() - 1; i >= 0; i --) {
            int num = partial.charAt(i) - '0';

            if(doubleCheck) {
                sum += num * 2;
            } else {
                sum += num;
            }

            doubleCheck = !doubleCheck;
        }

        return String.valueOf(sum % 10 == 0 ? 0 : 10 - sum % 10);
    }

    /* cvc 번호 발급 */
    public String generateCvc() {
        int cvc = random.nextInt(1000);
        return String.format("%03d", cvc);
    }

}
