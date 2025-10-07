package org.solcation.solcation_be.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solcation.solcation_be.repository.CardRepository;
import org.solcation.solcation_be.repository.GroupMemberRepository;
import org.solcation.solcation_be.repository.TransactionRepository;
import org.solcation.solcation_be.repository.TravelRepository;
import org.springframework.stereotype.Component;

@Slf4j
@Component("pageAuth")
@RequiredArgsConstructor
public class PageAuth {
    private final GroupMemberRepository groupMemberRepository;
    private final TravelRepository travelRepository;
    private final TransactionRepository transactionRepository;
    private final CardRepository cardRepository;

    /* 그룹 멤버인지 확인 */
    public boolean canAccessGroup(Long groupPk, Long userPk) {
        return groupMemberRepository.existsByGroup_GroupPkAndUser_UserPkAndIsAcceptedTrueAndIsOutFalse(groupPk, userPk);
    }

    /* 그룹 여행인지 확인 */
    public boolean canAccessTravel(Long groupPk, Long travelPk) {
        return travelRepository.existsByGroup_GroupPkAndTpPk(groupPk, travelPk);
    }

    /* 이용 내역 확인 */
    public boolean canAccessTransaction(Long groupPk, Long transactionPk) {
        return transactionRepository.existsBySatPkAndGmPk_Group_GroupPk(transactionPk, groupPk);
    }

    /* 카드 확인 */
    public boolean canAccessCard(Long groupPk, Long cardPk, Long userPk) {
        return cardRepository.existsBySacPkAndSaPk_Group_GroupPkAndGmPk_User_UserPkAndCancellationFalse(cardPk, groupPk, userPk);
    }

    public boolean isAllowed(String groupPk, Long userPk, String travelPk, String transactionPk, String cardPk) {
        //그룹 메인 (groupPk)
        if(groupPk == null) return false;

        if(travelPk == null && transactionPk == null && cardPk == null) {
            log.info("groupAuth");
            return canAccessGroup(Long.valueOf(groupPk), userPk);
        }

        //여행 (groupPk, travelPk)
        if(transactionPk == null && cardPk == null) {
            log.info("travelAuth");
            return canAccessTravel(Long.valueOf(groupPk), Long.valueOf(travelPk));
        }

        //이용내역 (groupPk, transactionPk)
        if(travelPk == null && cardPk == null) {
            log.info("transactionAuth");
            return canAccessTransaction(Long.valueOf(groupPk), Long.valueOf(transactionPk));
        }

        //카드 (groupPk, cardPk)
        if(travelPk == null && transactionPk == null) {
            log.info("cardAuth");
            return canAccessCard(Long.valueOf(groupPk), Long.valueOf(cardPk), userPk);
        }

        return false;
    }
}
