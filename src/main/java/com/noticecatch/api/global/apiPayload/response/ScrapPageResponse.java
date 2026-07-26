package com.noticecatch.api.global.apiPayload.response;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.Map;

@Getter
public class ScrapPageResponse<T> extends SliceResponse<T> {

    private final Map<String, Integer> categoryCounts; // 카테고리별 카운트 통계 추가

    private ScrapPageResponse(Page<T> page, Map<String, Integer> categoryCounts) {
        super(page.getContent(), page.getNumber(), page.getSize(), !page.isLast());
        this.categoryCounts = categoryCounts;
    }

    public static <T> ScrapPageResponse<T> of(Page<T> page, Map<String, Integer> categoryCounts) {
        return new ScrapPageResponse<>(page, categoryCounts);
    }
}
