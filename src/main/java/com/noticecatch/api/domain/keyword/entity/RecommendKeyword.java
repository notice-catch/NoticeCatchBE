package com.noticecatch.api.domain.keyword.entity;

import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
public enum RecommendKeyword {

    SCHOLARSHIP("장학금", "RECOMMEND"),
    EXTRACURRICULAR("비교과", "RECOMMEND"),
    ACADEMIC("학사", "RECOMMEND"),
    EMPLOYMENT("취업", "RECOMMEND"),
    OUTSIDE_ACTIVITY("대외활동", "RECOMMEND"),
    EXCHANGE_STUDENT("교환학생", "RECOMMEND");

    private final String keyword;
    private final String keywordType;

    public UserKeywordResponse toDto() {
        return new UserKeywordResponse(this.keyword, this.keywordType);
    }

    public static List<UserKeywordResponse> getRecommendList() {
        return Arrays.stream(RecommendKeyword.values())
                .map(RecommendKeyword::toDto)
                .toList();
    }
}
