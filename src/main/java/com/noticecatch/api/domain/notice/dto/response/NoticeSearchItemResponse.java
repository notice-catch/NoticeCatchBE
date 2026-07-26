package com.noticecatch.api.domain.notice.dto.response;

import com.noticecatch.api.domain.notice.entity.Notice;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeSearchItemResponse {

    private Long noticeId;
    private String categoryTag;
    private String title;
    private String source;
    private LocalDateTime createdAt; // postedAt 매핑
    private LocalDateTime deadlineAt;

    public static NoticeSearchItemResponse from(Notice notice) {
        if (notice == null) {
            return null;
        }

        String sourceName = "대학 본부";
        if (notice.getDepartment() != null) {
            sourceName = notice.getDepartment().getName();
        } else if (notice.getUniversity() != null) {
            sourceName = notice.getUniversity().getName();
        }

        return NoticeSearchItemResponse.builder()
                .noticeId(notice.getId())
                .categoryTag(notice.getCategory() != null ? notice.getCategory().getName() : "기타")
                .title(notice.getTitle())
                .source(sourceName)
                .createdAt(notice.getPostedAt()) // notice.getCreatedAt() -> notice.getPostedAt() 수정
                .deadlineAt(notice.getDeadlineAt())
                .build();
    }
}