package org.solcation.solcation_be.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //요청에서 추출
        String token = resolveBearer(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                if (!jwtTokenProvider.isExpired(token)) {
                    Number userPkNum = claims.get("userPk", Number.class);
                    Long userPk = userPkNum != null ? userPkNum.longValue() : null;

                    String userId = claims.getSubject();
                    String userName = claims.get("userName", String.class);
                    String email = claims.get("email", String.class);
                    String tel = claims.get("tel", String.class);

                    @SuppressWarnings("unchecked")
                    var raw = claims.get("roles", List.class);
                    List<String> roles = (raw == null ? List.of("ROLE_USER") : (List<String>) raw.stream().map(String::valueOf).toList());

                    var authorities = roles.stream().filter(Objects::nonNull).map(SimpleGrantedAuthority::new).toList();

                    JwtPrincipal principal = new JwtPrincipal(userPk, userId, userName, email, tel, roles);

                    Authentication auth = new JwtUserAuthentication(principal, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (io.jsonwebtoken.ExpiredJwtException ex) {
                log.info("JWT expired", ex);
            } catch (Exception ex) {
                 log.warn("JWT parsing/authentication failed: {}", ex.toString());
            }
        }
        filterChain.doFilter(request, response);
    }

    //Authorization 헤더에서 Bearer <token> 추출
    private String resolveBearer(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    // 인증 객체
    static class JwtUserAuthentication extends AbstractAuthenticationToken {
        private final JwtPrincipal principal; // userId

        JwtUserAuthentication(JwtPrincipal principal, Collection<SimpleGrantedAuthority> authorities) {
            super(authorities);
            this.principal = principal;
            setAuthenticated(true);
        }

        @Override public Object getCredentials() { return ""; }
        @Override public Object getPrincipal() { return principal; }
    }
}
