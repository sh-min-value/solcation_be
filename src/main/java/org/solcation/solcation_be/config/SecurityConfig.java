package org.solcation.solcation_be.config;

import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JsonSecurityHandlers handlers;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, PageAuth pageAuth) throws Exception {
        var pp = PathPatternRequestMatcher.withDefaults();
        RequestMatcher groupMatcher = pp.matcher("/api/group/{groupId:\\d+}/**");
        RequestMatcher TravelMatcher = pp.matcher("/api/group/{groupId:\\d+}/travel/{tpPk:\\d+}/**");
        RequestMatcher TransactionMatcher = pp.matcher("/api/group/{groupId:\\d+}/account/transaction/{satPk:\\d+}/**");
        RequestMatcher CardMatcher = pp.matcher("/api/group/{groupId:\\d+}/account/card/{sacPk:\\d+}/**");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.addAllowedOrigin("*");
                    config.addAllowedMethod("*");
                    config.addAllowedHeader("*");

                    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                    source.registerCorsConfiguration("/**", config);

                    cors.configurationSource(source);
                })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger & 헬스
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                        // 인증 발급/회원가입/소셜 콜백 등 공개
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        // 알림 허용
                        .requestMatchers("/api/notification/conn").permitAll()

                        //여행 인증
                        .requestMatchers(TravelMatcher).access(new GroupAuthorizationManager(pageAuth))
                        //트랜잭션 인증
                        .requestMatchers(TransactionMatcher).access(new GroupAuthorizationManager(pageAuth))
                        //카드 인증
                        .requestMatchers(CardMatcher).access(new GroupAuthorizationManager(pageAuth))
                        //그룹 멤버 인증
                        .requestMatchers(groupMatcher).access(new GroupAuthorizationManager(pageAuth))
                        // WebSocket 핸드셰이크 허용
                        .requestMatchers("/ws").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // 그 외는 인증 필요
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(handlers.authenticationEntryPoint())
                        .accessDeniedHandler(handlers.accessDeniedHandler())
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);



        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
