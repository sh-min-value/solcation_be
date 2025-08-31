package org.solcation.solcation_be.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    //커스텀 예외: throw new CustomException(ErrorCode.USER_NOT_FOUND)
    @ExceptionHandler(value = {CustomException.class})
    public ApiResponse<?> handleCustomException(CustomException e) {
        log.error("handleCustomException() in GlobalExceptionHandler throw CustomException: {}", e.getMessage());
        return ApiResponse.fail(e);
    }

    //40001 유효성 검증 검증 예외
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            ConstraintViolationException.class
    })
    public ApiResponse<?> handleValidation(Exception e) {
        log.error("Validation Failed {}", e.getMessage());

        Map<String, String> errors = new LinkedHashMap<>();
        if(e instanceof MethodArgumentNotValidException manv) {
            manv.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        }else if (e instanceof BindException be) {
            be.getBindingResult().getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
        } else if (e instanceof ConstraintViolationException cve) {
            cve.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        }

        return ApiResponse.fail(ErrorCode.TEST_ERROR, "유효성 검증 실패", errors);
    }

    // 40002 JSON 파싱/타입 변환
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<?> handleParse(HttpMessageNotReadableException e) {
        log.warn("JSON parse error", e);
        String msg = (e.getMostSpecificCause() != null) ? e.getMostSpecificCause().getMessage() : e.getMessage();
        return ApiResponse.fail(ErrorCode.JSON_PARSE_ERROR, "JSON 파싱 오류: " + msg);
    }

    // 40100 인증 오류
    @ExceptionHandler(AuthenticationException.class)
    public ApiResponse<?> handleAuth(AuthenticationException e) {
        log.warn("Unauthorized", e);

        return ApiResponse.fail(ErrorCode.UNAUTHORIZED);
    }

    // 40300 인가 오류
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<?> handleForbidden(AccessDeniedException e) {
        log.warn("Forbidden", e);

        return ApiResponse.fail(ErrorCode.FORBIDDEN);
    }

    // 40400 Not Found
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<?> handleNotFound(NoHandlerFoundException e) {
        log.warn("No handler found: {}", e.getRequestURL());

        return ApiResponse.fail(ErrorCode.NOT_FOUND_END_POINT);
    }

    // 40500 Method Not Allowed
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<?> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("Method not allowed: {}", e.getMethod());

        return ApiResponse.fail(ErrorCode.METHOD_NOT_ALLOWED);
    }

    //500 예기치 못한 오류
    @ExceptionHandler(value = {Exception.class})
    public ApiResponse<?> handleException(Exception e) {
        log.error("handleException() in GlobalExceptionHandler throw Exception: {}", e.getMessage());
        e.printStackTrace();
        return ApiResponse.fail(new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));
    }
}
