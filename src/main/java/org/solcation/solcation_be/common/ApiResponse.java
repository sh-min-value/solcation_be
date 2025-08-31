package org.solcation.solcation_be.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

public record ApiResponse<T>(
        @JsonIgnore HttpStatus httpStatus,
        boolean success,
        @Nullable T data,
        @Nullable ExceptionDTO error
) {
    public static <T> ApiResponse<T> ok(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ApiResponse<T> created(@Nullable final T data) {
        return new ApiResponse<>(HttpStatus.CREATED, true, data, null);
    }

    /* 일반 오류 */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(errorCode.getHttpStatus(), false, null, ExceptionDTO.of(errorCode));
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getHttpStatus(), false, null, ExceptionDTO.of(errorCode));
    }

    /* 비즈니스 로직 내 오류 */
    public static <T> ApiResponse<T> fail(ErrorCode errorCode, String message, Object detail) {
        return new ApiResponse<>(errorCode.getHttpStatus(), false, null, ExceptionDTO.of(errorCode, message, detail));
    }

    public static <T> ApiResponse<T> fail(final CustomException e) {
        return new ApiResponse<>(e.getErrorCode().getHttpStatus(), false, null, ExceptionDTO.of(e.getErrorCode()));
    }
}
