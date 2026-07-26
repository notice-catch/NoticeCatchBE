package com.noticecatch.api.domain.notification.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    PUSH_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "PUSH4001", "유효하지 않은 디바이스 토큰 형식입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}