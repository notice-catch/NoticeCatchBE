package com.noticecatch.api.domain.university.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UniversityErrorCode implements BaseErrorCode {

    UNIVERSITY_LIST_EMPTY(HttpStatus.NOT_FOUND, "UNIV4041",
            "대학 정보가 시스템에 존재하지 않습니다."),

    UNIVERSITY_NOT_FOUND(HttpStatus.NOT_FOUND,
            "UNIV4042", "유효하지 않거나 존재하지 않는 대학 식별자입니다."),

    UNIVERSITY_ALREADY_EXISTS(HttpStatus.CONFLICT,
            "UNIV4091", "이미 대학교 정보가 등록된 사용자입니다. " +
            "변경을 원하시면 프로필 수정을 이용해 주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
