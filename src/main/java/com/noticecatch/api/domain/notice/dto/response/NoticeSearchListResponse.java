package com.noticecatch.api.domain.notice.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NoticeSearchListResponse {

    private List<NoticeSearchItemResponse> content;
    private int page;
    private int size;
    private boolean hasNext;

    public static NoticeSearchListResponse of(Slice<NoticeSearchItemResponse> slice) {
        if (slice == null) {
            return null;
        }

        return NoticeSearchListResponse.builder()
                .content(slice.getContent())
                .page(slice.getNumber())
                .size(slice.getSize())
                .hasNext(slice.hasNext())
                .build();
    }
}