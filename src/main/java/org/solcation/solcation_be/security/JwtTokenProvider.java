package org.solcation.solcation_be.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.access-token-validity-ms}")
    private long accessValidityMs;

    @Value("${security.jwt.refresh-token-validity-ms}")
    private long refreshValidityMs;

    private SecretKey key;

    // 문자열 시크릿 키 -> HMAC용 시크릿 키로 변환
    @PostConstruct
    void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // Access Token 생성(subject(userId), 컨트롤러, 서비스에서 바로 필요한 유저 정보들)
    public String createAccessToken(String subject, Map<String, Object> claims) {
        return createToken(subject, claims, accessValidityMs);
    }

    // Refresh Token 생성
    public String createRefreshToken(String subject) {
        return createToken(subject, Map.of("type", "refresh"), refreshValidityMs);
    }

    public String createToken(String subject, Map<String, Object> claims, long validityMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);
        return Jwts.builder()
                .issuer("solcation")
                .audience().add("web").and()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .claims(claims)
                .issuedAt(now)
                .expiration(exp)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    // 서명 검증 + 확인
    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 만료 여부 확인
    public boolean isExpired(String token) {
        try {
            var claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            return true;
        }
    }
}
