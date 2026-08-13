package com.noticecatch.api.domain.keyword.controller;

import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "🏷️ Keyword", description = "추천 키워드 제공 API")
public interface KeywordControllerDocs {
    @Operation(summary = "추천 키워드 목록 조회", description = "시스템이 기본 제공하는 추천 키워드 리스트를 가져옵니다.")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공 예시 (COMMON200)",
                                    value = """
                    {
                      "isSuccess": true,
                      "code": "COMMON200",
                      "message": "성공",
                      "result": {
                        "content": [
                          { "keyword": "장학금", "keywordType": "RECOMMEND" },
                          { "keyword": "비교과", "keywordType": "RECOMMEND" },
                          { "keyword": "학사", "keywordType": "RECOMMEND" },
                          { "keyword": "취업", "keywordType": "RECOMMEND" },
                          { "keyword": "대외활동", "keywordType": "RECOMMEND" },
                          { "keyword": "교환학생", "keywordType": "RECOMMEND" }
                        ],
                        "totalCount": 6
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 에러",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (SERVER500)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "SERVER500",
                      "message": "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<UserKeywordResponse>> getRecommendKeywords(
            @CurrentUserId Long userId);
}
