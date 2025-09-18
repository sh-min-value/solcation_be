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
    private final GroupAuth groupAuth;
    private final TravelAuth travelAuth;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        //유저 정보 꺼냄
        Authentication auth = authentication.get();
        if(auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        var principal = (JwtPrincipal) auth.getPrincipal();
        Long userPk = principal.userPk();

        //url에서 groupId 추출
        String gid = object.getVariables().get("groupId");
        if(gid == null) {
            log.info("groupAuth: groupId is null");
            return new AuthorizationDecision(false);
        }

        //그룹 멤버 여부 확인
        boolean isAllowedGroup = groupAuth.memberOf(Long.valueOf(gid), userPk);

        //url에서 travelId 추출
        String tid = object.getVariables().get("tpPk");

        //여행 관련 api가 아닌 경우
        if(tid == null) {
            log.info("groupAuth: {} {}", isAllowedGroup, gid);
            return new AuthorizationDecision(isAllowedGroup);
        }

        boolean isAllowedTravel = travelAuth.canAccessTravel(Long.valueOf(gid), Long.valueOf(tid));
        boolean finalAllowed = isAllowedGroup && isAllowedTravel;

        log.info("groupAndTravelAuth: {} {}", finalAllowed, gid);
        return new AuthorizationDecision(finalAllowed);
    }
}
