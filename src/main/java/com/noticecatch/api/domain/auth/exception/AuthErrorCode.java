package com.noticecatch.api.domain.auth.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH401", "유효하지 않거나 만료된 리프레시 토큰입니다. 다시 로그인해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
