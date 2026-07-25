package com.noticecatch.api.domain.department.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DepartmentErrorCode implements BaseErrorCode {

    DEPARTMENT_NOT_FOUND_BY_KEYWORD(HttpStatus.NOT_FOUND, "DEPT4041", "해당 학과 정보가 시스템에 존재하지 않습니다. 입력한 이름을 다시 확인해 주세요."),
    UNIVERSITY_DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPT4042", "선택하신 대학교에 개설된 학과 정보를 찾을 수 없습니다."),
    DEPARTMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "DEPT4043", "유효하지 않거나 존재하지 않는 학과 식별자(ID)입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
