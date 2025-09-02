package org.solcation.solcation_be.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.solcation.solcation_be.common.ApiResponse;
import org.solcation.solcation_be.common.ErrorCode;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletResponse;

@Component
@RequiredArgsConstructor
public class JsonSecurityHandlers {

    private final ObjectMapper om = new ObjectMapper();

    /* Unauthorized exception */
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, ex) -> writeJson(response, HttpStatus.UNAUTHORIZED, ApiResponse.fail(ErrorCode.UNAUTHORIZED));
    }

    /* Forbidden exception */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, ex) -> writeJson(response, HttpStatus.FORBIDDEN, ApiResponse.fail(ErrorCode.FORBIDDEN));
    }

    private void writeJson(HttpServletResponse res, HttpStatus status, Object body) throws java.io.IOException {
        res.setStatus(status.value());
        res.setContentType("application/json;charset=UTF-8");
        om.writeValue(res.getWriter(), body);
    }
}
