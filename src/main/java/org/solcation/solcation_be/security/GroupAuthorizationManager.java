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

@Slf4j
@RequiredArgsConstructor
public class GroupAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
    private final GroupAuth groupAuth;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        //유저 정보 꺼냄
        Authentication auth = authentication.get();
        if(auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        var principal = (JwtPrincipal) auth.getPrincipal();
        Long userPk = principal.userPk();

        var request = object.getRequest();

        //url에서 변수 추출
        String gid = object.getVariables().get("groupId");
        if(gid == null) {
            return new AuthorizationDecision(false);
        }

        //그룹 멤버 여부 확인
        boolean isAllowed = groupAuth.memberOf(Long.valueOf(gid), userPk);

        log.info("groupAuth: {} {}", isAllowed, gid);
        return new AuthorizationDecision(isAllowed);
    }
}
