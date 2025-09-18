package org.solcation.solcation_be.common;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 모든 응답 대상
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType contentType,
                                  Class<? extends HttpMessageConverter<?>> converterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if(body == null) {
            HttpStatus current = HttpStatus.OK;

            //상태 코드 추출 (null을 전송하는 경우)
            if(response instanceof ServletServerHttpResponse servlet) {
                HttpStatus resoleved = HttpStatus.resolve(servlet.getServletResponse().getStatus());

                if(resoleved != null) {
                    current = resoleved;
                }
            }

            if(current == HttpStatus.NO_CONTENT) {
                return null;
            }

            return ApiResponse.ok(null);
        }

        if (body instanceof ApiResponse) {
            HttpStatus status = ((ApiResponse<?>) body).httpStatus();
            response.setStatusCode(status);
        }

        return body;
    }
}
