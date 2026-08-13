package com.noticecatch.api.domain.department.controller;

import com.noticecatch.api.domain.department.dto.response.DepartmentResponse;
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
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "🎓 Department", description = "학과 조회 및 검색 API")
public interface DepartmentControllerDocs {
    @Operation(summary = "학과 목록 조회 및 검색", description = "특정 대학에 개설된 학과 목록을 검색 키워드 기반으로 조회합니다.")
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
                          {
                            "departmentId": 1,
                            "departmentName": "컴퓨터공학과"
                          },
                          {
                            "departmentId": 2,
                            "departmentName": "소프트웨어학과"
                          }
                        ],
                        "totalCount": 2
                      }
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
                                    name = "실패 예시 (DEPT4041)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "DEPT4041",
                      "message": "해당 학과 정보가 시스템에 존재하지 않습니다. 입력한 이름을 다시 확인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<DepartmentResponse>> getDepartments(
            @CurrentUserId Long userId,
            @Parameter(description = "조회 대상 대학의 고유 ID", example = "1")
            @PathVariable Long universityId,
            @Parameter(description = "학과 검색어", example = "컴퓨터")
            @RequestParam(name = "keyword", required = false) String keyword
    );
}
