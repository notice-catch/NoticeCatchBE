package com.noticecatch.api.domain.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class NoticeScrapResponse {
    private Long noticeId;
    private Boolean isScraped;

    public static NoticeScrapResponse of(Long noticeId, Boolean isScraped) {
        return NoticeScrapResponse.builder()
                .noticeId(noticeId)
                .isScraped(isScraped)
                .build();
    }
}