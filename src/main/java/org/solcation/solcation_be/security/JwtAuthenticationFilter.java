package org.solcation.solcation_be.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveBearer(request);

        if (StringUtils.hasText(token)) {
            try {
                Claims claims = jwtTokenProvider.parseClaims(token);
                if (!jwtTokenProvider.isExpired(token)) {
                    String userId = claims.getSubject();

                    @SuppressWarnings("unchecked")
                    var roles = (Collection<String>) claims.getOrDefault("roles", java.util.List.of("ROLE_USER"));
                    var authorities = roles.stream().filter(Objects::nonNull)
                            .map(SimpleGrantedAuthority::new).collect(Collectors.toList());
                    Authentication auth = new JwtUserAuthentication(userId, authorities);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            } catch (Exception ignore) {

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
        private final String principal; // userId

        JwtUserAuthentication(String principal, Collection<SimpleGrantedAuthority> authorities) {
            super(authorities);
            this.principal = principal;
            setAuthenticated(true);
        }

        @Override public Object getCredentials() { return ""; }
        @Override public Object getPrincipal() { return principal; }
    }
}
