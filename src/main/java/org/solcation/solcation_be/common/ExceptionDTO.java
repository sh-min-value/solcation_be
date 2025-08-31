package org.solcation.solcation_be.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ExceptionDTO {
    @NotNull
    private final Integer code;

    @NotNull
    private final String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Object detail;

    public ExceptionDTO(Integer code, String message, Object detail) {
        this.code = code;
        this.message = message;
        this.detail = detail;
    }
    public static ExceptionDTO of(ErrorCode ec) {
        return new ExceptionDTO(ec.getCode(), ec.getMessage(), null);
    }
    public static ExceptionDTO of(ErrorCode ec, String overrideMessage) {
        return new ExceptionDTO(ec.getCode(), overrideMessage, null);
    }
    public static ExceptionDTO of(ErrorCode ec, Object detail) {
        return new ExceptionDTO(ec.getCode(), ec.getMessage(), detail);
    }
    public static ExceptionDTO of(ErrorCode ec, String overrideMessage, Object detail) {
        return new ExceptionDTO(ec.getCode(), overrideMessage, detail);
    }
}
