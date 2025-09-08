package org.solcation.solcation_be.security;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.CustomException;
import org.solcation.solcation_be.common.ErrorCode;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwt; // 기존 서비스

    @Override
    public Message<?> preSend(Message<?> msg, MessageChannel channel) {
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(msg, StompHeaderAccessor.class);
        if (acc == null) return msg;

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String auth = acc.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new CustomException(ErrorCode.UNAUTHORIZED);
            }

            String token = auth.substring(7);
            Claims claims = jwt.parseClaims(token);
            if (jwt.isExpired(token)) {
                throw new CustomException(ErrorCode.TOKEN_EXPIRED);
            }

            List<String> roles = extractRoles(claims);

            var authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            Long userPk = toLong(claims.get("userPk"));
            String userId = claims.getSubject();
            String userName = toStringOrNull(claims.get("userName"));
            String email    = toStringOrNull(claims.get("email"));
            String tel      = toStringOrNull(claims.get("tel"));

            JwtPrincipal principal = new JwtPrincipal(userPk, userId, userName, email, tel, roles);

            Authentication authn =
                    new UsernamePasswordAuthenticationToken(principal, "", authorities);
            acc.setUser(authn);
        }
        return msg;
    }
    private static List<String> extractRoles(Claims claims) {
        Object o = claims.get("roles");
        if (o instanceof Collection<?> c) {
            return c.stream().map(String::valueOf).toList();
        }
        if (o instanceof String s && !s.isBlank()) {
            // "ROLE_USER,ROLE_ADMIN" 같은 형태도 지원하려면 split 처리
            return s.contains(",") ? List.of(s.split("\\s*,\\s*")) : List.of(s);
        }
        return List.of("ROLE_USER");
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }

    private static String toStringOrNull(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
