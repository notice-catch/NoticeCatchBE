package com.noticecatch.api.domain.notice.exception;

import com.noticecatch.api.global.apiPayload.code.BaseErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements BaseErrorCode {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTICE4041", "해당 공지사항이 존재하지 않거나 삭제되었습니다."),
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "SEARCH4001", "검색 키워드는 공백을 제외한 최소 2글자 이상 입력해야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}