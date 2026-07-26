package com.noticecatch.api.domain.notice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CalendarDatesResponse {

    private List<String> content;
    private int totalCount;

    public static CalendarDatesResponse of(List<String> dates) {
        return CalendarDatesResponse.builder()
                .content(dates)
                .totalCount(dates != null ? dates.size() : 0)
                .build();
    }
}