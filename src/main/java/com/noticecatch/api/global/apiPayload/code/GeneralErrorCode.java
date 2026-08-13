package com.noticecatch.api.global.apiPayload.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode{
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
            "COMMON500", "서버 내부 오류가 발생했습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST,
            "COMMON400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,
            "COMMON401", "인증되지 않았습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,
            "COMMON403", "접근이 금지되었습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,
            "COMMON404", "해당 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED,
            "COMMON405", "지원하지 않는 HTTP 메소드입니다."),
    CONFLICT(HttpStatus.CONFLICT,
            "COMMON409", "요청이 현재 리소스 상태와 충돌합니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;;
}
