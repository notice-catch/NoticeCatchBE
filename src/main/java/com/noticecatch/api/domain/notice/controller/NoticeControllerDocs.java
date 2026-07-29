package com.noticecatch.api.domain.notice.controller;

import com.noticecatch.api.domain.notice.dto.response.*;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "📢 Notice", description = "학사 공지사항 조회, 검색, 스크랩, 캘린더 API")
@SuppressWarnings("unused")
public interface NoticeControllerDocs {

    @Operation(summary = "홈 공지 피드", description = "통합 공지 피드 및 카테고리 필터링 조회를 지원합니다. page=0이면 최신 동기화.")
    @Parameters({
            @Parameter(name = "page", description = "페이지 번호 (0부터 시작)", example = "0"),
            @Parameter(name = "size", description = "한 페이지당 출력 개수", example = "20"),
            @Parameter(name = "keyword", description = "필터 카테고리 혹은 키워드 명칭", example = "학사")
    })
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                            "noticeId": 1,
                            "categoryTag": "학사",
                            "title": "2026학년도 2학기 수강신청 기간 안내",
                            "source": "컴퓨터공학과 사무실",
                            "createdAt": "2026-07-01T09:00:00",
                            "deadlineAt": "2026-07-09T23:59:59"
                          }
                        ],
                        "page": 0,
                        "size": 20,
                        "hasNext": true
                      }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (NOTICE4001)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "NOTICE4001",
                      "message": "유효하지 않은 공지사항 카테고리 필터 요청입니다.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<SliceResponse<NoticeSearchItemResponse>> getNotices(
            @CurrentUserId Long userId,
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    );

    @Operation(summary = "공지 상세 보기", description = "AI 요약 내용이 결합된 공지사항 세부 내용을 가져옵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                        "noticeId": 42,
                        "categoryTag": "학사",
                        "title": "2026학년도 2학기 수강신청 기간 안내",
                        "source": "컴퓨터공학과 사무실",
                        "createdAt": "2026-07-01T09:00:00",
                        "deadlineAt": "2026-07-09T23:59:59",
                        "content": "안녕하세요. 컴퓨터공학과 사무실입니다. 수강신청 일정을 다음과 같이 안내하오니...",
                        "hasFiles": true,
                        "originalUrl": "https://www.university.ac.kr/notice/12345",
                        "aiSummary": {
                          "eligibility": "직전 학기 12학점 이상 이수 및 이수 평점 3.5 이상인 자",
                          "benefit": "등록금 전액 면제 및 도서비 분기별 20만 원 지급",
                          "deadline": "2026년 7월 9일 18:00까지 (오프라인 제출 불가)"
                        }
                      }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "조회 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (NOTICE4041)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "NOTICE4041",
                      "message": "해당 공지사항이 존재하지 않거나 삭제되었습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<NoticeDetailResponse> getNoticeDetail(
            @CurrentUserId Long userId,
            @PathVariable @Parameter(description = "공지사항 고유 ID", example = "42") Long noticeId
    );

    @Operation(summary = "키워드 검색", description = "검색어 및 정렬 기준에 맞춰 공지사항 목록을 무한 스크롤 페이징합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                            "noticeId": 612,
                            "categoryTag": "장학",
                            "title": "이공계 국가우수장학금 추가 선발 안내",
                            "source": "학생복지처",
                            "createdAt": "2026-06-25T10:00:00",
                            "deadlineAt": "2026-07-09T23:59:59"
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<NoticeSearchListResponse> searchNotices(
            @Parameter(description = "검색 키워드 (최소 2자)", example = "장학") @RequestParam String searchWord,
            @Parameter(description = "정렬 기준 (latest: 최신순, deadline: 마감일순)", example = "latest") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam int page,
            @Parameter(description = "한 페이지당 출력 개수", example = "20") @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "공지 스크랩 설정/해제", description = "공지사항 단건을 스크랩하거나 취소합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                        "noticeId": 42,
                        "isScraped": true
                      }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "설정 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (NOTICE4041)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "NOTICE4041",
                      "message": "존재하지 않거나 이미 삭제된 공지사항은 스크랩할 수 없습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<NoticeScrapResponse> scrapNotice(
            @CurrentUserId Long userId,
            @PathVariable @Parameter(description = "스크랩할 공지사항 고유 ID", example = "42") Long noticeId
    );

    @Operation(summary = "스크랩 목록 보기", description = "카테고리별 누적 카운팅 정보를 포함하여 스크랩 리스트를 페이징 반환합니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                        "categoryCounts": {
                          "ALL": 12,
                          "SCHOLARSHIP": 5,
                          "ACADEMIC": 4,
                          "EMPLOYMENT": 3
                        },
                        "content": [
                          {
                            "noticeId": 42,
                            "categoryTag": "장학",
                            "title": "[당일마감🔥] 2026학년도 2학기 교내 성적우수 장학금 신청",
                            "source": "학생복지처",
                            "createdAt": "2026-07-01T09:00:00",
                            "deadlineAt": "2026-07-04T18:00:00"
                          },
                          {
                            "noticeId": 105,
                            "categoryTag": "학사",
                            "title": "2026학년도 2학기 전과 신청 및 선발 일정 안내",
                            "source": "교무처",
                            "createdAt": "2026-07-02T14:00:00",
                            "deadlineAt": "2026-07-10T23:59:59"
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<NoticeScrapListResponse> getScrapNotices(
            @CurrentUserId Long userId,
            @RequestParam(required = false) String categoryTag,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "캘린더 스크랩 마감 공지 날짜 조회", description = "특정 년/월에 마감 일정이 존재하는 날짜 리스트를 뽑아옵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                          "2026-07-04",
                          "2026-07-09",
                          "2026-07-15"
                        ],
                        "totalCount": 3
                      }
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<CalendarDatesResponse> getCalendarDates(
            @CurrentUserId Long userId,
            @RequestParam String year,
            @RequestParam String month
    );

    @Operation(summary = "선택한 날짜의 마감 공지 목록 조회", description = "선택 날짜 기한에 매칭되는 마감 공지 목록을 불러옵니다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
                            "noticeId": 42,
                            "categoryTag": "장학",
                            "title": "[당일마감🔥] 2026학년도 2학기 교내 성적우수 장학금 신청",
                            "source": "학생복지처",
                            "createdAt": "2026-07-01T09:00:00",
                            "deadlineAt": "2026-07-04T18:00:00"
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
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<SliceResponse<NoticeSearchItemResponse>> getCalendarNotices(
            @CurrentUserId Long userId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd) 기입", example = "2026-07-04") @RequestParam String date,
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(
            summary = "마감일이 없는(기한 없음) 공지 목록 조회",
            description = "사용자가 스크랩한 공지 중 마감 기한이 없는(deadlineAt이 null인) 공지 목록을 불러옵니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "성공 예시 (COMMON200)",
                                            value = """
                        {
                          "isSuccess": true,
                          "code": "COMMON200",
                          "message": "성공",
                          "result": {
                            "content": [
                              {
                                "noticeId": 99,
                                "categoryTag": "일반",
                                "title": "교내 흡연구역 부스 리모델링 공사 안내 (기한없음)",
                                "source": "시설관리팀",
                                "createdAt": "2026-07-02T11:00:00",
                                "deadlineAt": null
                              }
                            ],
                            "page": 0,
                            "size": 20,
                            "hasNext": false
                          }
                        }
                        """
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (AUTH401)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "AUTH401",
                      "message": "인증 자격 증명이 유효하지 않거나 만료되었습니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
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
    ApiResponse<SliceResponse<NoticeSearchItemResponse>> getNoDeadlineNotices(
            @CurrentUserId Long userId,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam int page,
            @Parameter(description = "한 페이지에 보여줄 개수", example = "20") @RequestParam(defaultValue = "20") int size
    );
}