package com.noticecatch.api.domain.department.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepartmentErrorCode implements BaseErrorCode {

    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPT4043", "유효하지 않거나 존재하지 않는 학과 식별자(ID)입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
