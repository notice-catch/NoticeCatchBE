package com.noticecatch.api.domain.university.controller;

import com.noticecatch.api.domain.university.dto.response.UniversityResponse;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;

@Tag(name = "🏫 University", description = "대학 정보 제공 및 선택 API")
public interface UniversityControllerDocs {
    @Operation(summary = "대학 목록 조회", description = "온보딩 대학 선택을 위한 전체 대학 리스트를 조회합니다.")
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
                          { "universityId": 1, "universityName": "동아대학교" },
                          { "universityId": 2, "universityName": "영남대학교" },
                          { "universityId": 3, "universityName": "인제대학교" }
                        ],
                        "totalCount": 3
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
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<UniversityResponse>> getUniversities(@CurrentUserId Long userId);

    @Operation(summary = "대학 선택", description = "유저의 소속 대학을 등록 및 변경합니다.")
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
                      "message": "대학 선택이 성공적으로 완료되었습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (UNIV4042)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "UNIV4042",
                      "message": "유효하지 않거나 존재하지 않는 대학 식별자(ID)입니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<Void> selectUniversity(
            @CurrentUserId Long userId,
            @Parameter(description = "선택할 대학의 고유 ID", example = "1")
            @PathVariable Long universityId
    );
}