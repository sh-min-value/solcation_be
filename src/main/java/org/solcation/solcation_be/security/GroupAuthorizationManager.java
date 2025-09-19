package org.solcation.solcation_be.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Map;
import java.util.function.Supplier;

import static java.lang.Long.parseLong;

@Slf4j
@RequiredArgsConstructor
public class GroupAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final PageAuth pageAuth;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        //유저 정보 꺼냄
        Authentication auth = authentication.get();
        if(auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        var principal = (JwtPrincipal) auth.getPrincipal();
        Long userPk = principal.userPk();

        String groupPk = object.getVariables().get("groupId");
        String travelPk = object.getVariables().get("tpPk");
        String transactionPk = object.getVariables().get("satPk");
        String cardPk = object.getVariables().get("sacPk");

        boolean isAllowed = pageAuth.isAllowed(groupPk, userPk, travelPk, transactionPk, cardPk);

        log.info("PageAuth: {} {}", isAllowed, groupPk);
        return new AuthorizationDecision(isAllowed);
    }
}
