package com.noticecatch.api.domain.keyword.controller;

import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.domain.keyword.service.UserKeywordService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class KeywordController implements KeywordControllerDocs {
    private final UserKeywordService userKeywordService;

    @Override
    @GetMapping("/keywords/recommend")
    public ApiResponse<ListResponse<UserKeywordResponse>> getRecommendKeywords(
            @CurrentUserId Long  userId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                userKeywordService.getRecommendKeywords(userId));
    }
}
