package com.noticecatch.api.domain.support.controller;

import com.noticecatch.api.domain.support.dto.response.FaqResponse;
import com.noticecatch.api.domain.support.dto.response.SupportNoticeResponse;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "🛠️ Support", description = "운영 공지 및 FAQ 관리용 고객지원 API")
public interface SupportControllerDocs {

    @Operation(summary = "운영팀 공지사항 목록 조회", description = "시스템 정기점검 혹은 릴리즈 업데이트 공지사항 목록을 가져옵니다.")
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
                            "supportNoticeId": 5,
                            "title": "[공지] 시스템 정기 점검 안내 (7/15)",
                            "content": "안녕하세요. 서비스 안정화를 위한 시스템 점검이 진행될 예정입니다...",
                            "createdAt": "2026-07-04T12:00:00"
                          },
                          {
                            "supportNoticeId": 4,
                            "title": "[업데이트] 버전 1.2.0 출시 안내 (캘린더 기능 추가)",
                            "content": "드디어 많은 분들이 기다리시던 스크랩 캘린더 기능이 업데이트 되었습니다!",
                            "createdAt": "2026-07-01T09:00:00"
                          }
                        ],
                        "page": 0,
                        "size": 20,
                        "hasNext": false
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
    com.noticecatch.api.global.apiPayload.ApiResponse<SliceResponse<SupportNoticeResponse>> getSupportNotices(
            @RequestParam int page, @RequestParam int size);

    @Operation(summary = "FAQ 목록 조회", description = "선택 태그 범주에 속한 자주 묻는 질문들을 리스트업합니다.")
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
                            "faqId": 12,
                            "category": "ACCOUNT",
                            "question": "비밀번호를 분별하거나 변경하고 싶어요.",
                            "answer": "로그인 화면 하단의 '비밀번호 찾기'를 이용하시거나, 마이페이지 > 프로필 수정에서 변경 가능합니다."
                          },
                          {
                            "faqId": 15,
                            "category": "ACCOUNT",
                            "question": "회원 탈퇴는 어떻게 하나요?",
                            "answer": "마이페이지 > 설정 > 회원 탈퇴 메뉴를 통해 탈퇴를 진행하실 수 있으며, 탈퇴 즉시 스크랩 데이터는 삭제됩니다."
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
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<FaqResponse>> getFaqs(
            @Parameter(description = "FAQ 카테고리 태그명", example = "ACCOUNT") @RequestParam String category
    );
}
