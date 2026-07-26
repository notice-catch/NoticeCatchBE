package com.noticecatch.api.domain.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
public class NoticeScrapListResponse {

    private Map<String, Long> categoryCounts;
    private List<NoticeSearchItemResponse> content;
    private int page;
    private int size;
    private boolean hasNext;

    public static NoticeScrapListResponse of(Map<String, Long> categoryCounts, Slice<NoticeSearchItemResponse> slice) {
        return NoticeScrapListResponse.builder()
                .categoryCounts(categoryCounts)
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}