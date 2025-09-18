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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtTokenProvider jwtTokenProvider;
    private final JsonSecurityHandlers handlers;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, GroupAuth groupAuth, TravelAuth travelAuth) throws Exception {
        var pp = PathPatternRequestMatcher.withDefaults();
        RequestMatcher groupMatcher = pp.matcher("/group/{groupId:\\d+}/**");
        RequestMatcher groupAndTravelMatcher = pp.matcher("/group/{groupId:\\d+}/travel/{tpPk:\\d+}");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Swagger & 헬스
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health").permitAll()
                        // 인증 발급/회원가입/소셜 콜백 등 공개
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/auth/**").permitAll()
                        //그룹 멤버 & 여행 인증
                        .requestMatchers(groupAndTravelMatcher).access(new GroupAuthorizationManager(groupAuth, travelAuth))
                        //그룹 멤버 인증
                        .requestMatchers(groupMatcher).access(new GroupAuthorizationManager(groupAuth, travelAuth))
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
