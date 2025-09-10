package org.solcation.solcation_be.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 테스트 에러
    TEST_ERROR(10000, HttpStatus.BAD_REQUEST, "테스트 에러입니다."),

    // 400 Bad Request
    BAD_REQUEST(40000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."), //DEFAULT
    VALIDATION_FAILED(40001, HttpStatus.BAD_REQUEST, "유효성 검증이 실패하였습니다."),
    JSON_PARSE_ERROR(40002, HttpStatus.BAD_REQUEST, "JSON 파싱 중 오류가 발생하였습니다."),
    UNKNOWN_MOD_TYPE(40003, HttpStatus.BAD_REQUEST, "알 수 없는 수정 요청입니다."),

    // 401 Unauthorized
    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "인증이 필요합니다."), //DEFAULT
    USER_NOT_FOUND(40101, HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 일치하지 않습니다."),
    TOKEN_EXPIRED(40102, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),

    // 403 Forbidden
    FORBIDDEN(40300, HttpStatus.FORBIDDEN, "권한이 없습니다."), //DEFAULT

    // 404 Not Found
    NOT_FOUND_END_POINT(40400, HttpStatus.NOT_FOUND, "존재하지 않는 API입니다."), //DEFAULT
    NOT_FOUND_ACCOUNT(40401, HttpStatus.NOT_FOUND, "계좌가 존재하지 않습니다."),
    NOT_FOUND_PLAN(40403, HttpStatus.NOT_FOUND, "여행 계획이 없습니다."),
    NOT_FOUND_CATEGORY(40404, HttpStatus.NOT_FOUND, "존재하지 않는 카테고리입니다"),
    NOT_FOUND_GROUP(40405, HttpStatus.NOT_FOUND, "존재하지 않는 세부 계획입니다"),

    // 405 Method Not Allowed
    METHOD_NOT_ALLOWED(40500, HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."), //DEFAULT

    // 408 Request Timeout
    REQEUST_TIMEOUT(40800, HttpStatus.REQUEST_TIMEOUT, "요청 시간이 초과했습니다."), //DEFAULT

    // 409 Conflict
    CONFLICT(40900, HttpStatus.CONFLICT, "자원이 충돌하였습니다."), //DEFAULT
    ACCOUNT_ALREADY_EXISTS(40902, HttpStatus.CONFLICT, "계좌가 이미 존재합니다."),

    // 410 Gone
    GONE(41000, HttpStatus.GONE, "만료되었습니다."),

    // 413 Payload Too Large
    PAYLOAD_TOO_LARGE(41300, HttpStatus.PAYLOAD_TOO_LARGE, "용량이 너무 큽니다."), //DEFAULT

    // 415 Unsupported Media Type
    UNSUPPORTED_MEDIA_TYPE(41500, HttpStatus.UNSUPPORTED_MEDIA_TYPE, "지원하지 않는 파일 확장자입니다."), //DEFAULT

    // 423 Locked
    LOCKED(42300, HttpStatus.LOCKED, "자원에 접근할 수 없습니다."),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;
}