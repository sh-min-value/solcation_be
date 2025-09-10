package org.solcation.solcation_be.wallet.card;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.solcation.solcation_be.domain.wallet.card.CardService;
import org.solcation.solcation_be.domain.wallet.card.dto.OpenCardReqDTO;
import org.solcation.solcation_be.security.JwtPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class CardTests {
    @Autowired
    private CardService cardService;

//    @Test
//    public void openCardTest() {
//        JwtPrincipal jwtPrincipal = new JwtPrincipal(16L, "admin3", null, null, null, null);
//        cardService.openCard(16L, jwtPrincipal, OpenCardReqDTO.builder().pw("1234").build());
//    }
}
