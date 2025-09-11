package org.solcation.solcation_be.time;

import io.micrometer.core.annotation.TimedSet;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.solcation.solcation_be.entity.Card;
import org.solcation.solcation_be.entity.SharedAccount;
import org.solcation.solcation_be.entity.User;
import org.solcation.solcation_be.entity.enums.TRANSACTIONTYPE;
import org.solcation.solcation_be.repository.CardRepository;
import org.solcation.solcation_be.repository.SharedAccountRepository;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.UserRepository;
import org.solcation.solcation_be.util.timezone.ZonedTimeRange;
import org.solcation.solcation_be.util.timezone.ZonedTimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class TimeTests {
    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SharedAccountRepository sharedAccountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

//    @Test
//    public void test() {
//        // given: 2025-09-10 (KST 하루)
//        LocalDate day = LocalDate.of(2025, 9, 10);
//
//        // when
//        ZonedTimeRange r = ZonedTimeUtil.day(day);
//
//        // then: KST 00:00 → UTC 전날 15:00
//        assertEquals(Instant.parse("2025-09-09T15:00:00Z"), r.start());
//        assertEquals(Instant.parse("2025-09-10T15:00:00Z"), r.end());
//    }
//
//    @Test
//    public void test2() {
//        SharedAccount saPk = sharedAccountRepository.findByGroup_GroupPk(16L);
//        User user = userRepository.findByUserId("admin3").orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
//        Card sacPk = cardRepository.findBySacPk(1L);
//
//        ZonedTimeRange r = ZonedTimeUtil.day(LocalDate.of(2025, 9, 10));
//        List<Long> l = transactionRepository.findTotalAmountForPeriod2(saPk, TRANSACTIONTYPE.CARD, user, sacPk, r.start(), r.end());
//        l.forEach(i -> System.out.println(i));
//    }
}
