package com.noticecatch.api.domain.user.controller;

import com.noticecatch.api.domain.keyword.dto.request.UserKeywordUpdateRequest;
import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.domain.user.dto.request.AlarmUpdateRequest;
import com.noticecatch.api.domain.user.dto.request.ProfileUpdateRequest;
import com.noticecatch.api.domain.user.dto.response.AlarmSettingResponse;
import com.noticecatch.api.domain.user.dto.response.MyPageProfileResponse;
import com.noticecatch.api.domain.user.dto.response.ProfileResponse;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

@Tag(name = "⚙️ User & MyPage", description = "온보딩 세팅, 개인 프로필 관리 및 알림 제어 API")
public interface UserControllerDocs {

    @Operation(summary = "온보딩 프로필 설정", description = "온보딩 단계에서 학과 소속 식별 및 학년을 세팅합니다.")
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
                      "message": "학과 및 학년 등록이 성공적으로 완료되었습니다.",
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
                                    name = "실패 예시 (DEPT4043)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "DEPT4043",
                      "message": "유효하지 않거나 존재하지 않는 학과 식별자(ID)입니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<Void> setupOnboardingProfile(
            @CurrentUserId Long userId,
            @Parameter(description = "등록할 학과의 고유 ID", example = "1") @PathVariable Long departmentId,
            @RequestBody ProfileUpdateRequest request
    );

    @Operation(summary = "마이페이지 및 프로필 조회", description = "기본 인적사항 및 개인 지표 데이터를 추출합니다.")
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
                        "nickname": "소셜유저_a1b2c",
                        "universityName": "인제대학교",
                        "departmentName": "컴퓨터공학과",
                        "grade": 3,
                        "scrapCount": 12,
                        "keywordCount": 5,
                        "readCount": 47
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
                                    name = "실패 예시 (USER4041)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "USER4041",
                      "message": "존재하지 않거나 이미 탈퇴한 회원의 프로필입니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<MyPageProfileResponse> getProfile(
            @Parameter(hidden = true) @CurrentUserId Long userId
    );

    @Operation(summary = "프로필 수정", description = "마이페이지 환경 내 소속 대학 ID, 학과 ID, 학년 사안을 변경 및 조율합니다.")
    @Parameters({
            @Parameter(name = "universityId", description = "수정 변경할 대학교 고유 ID", example = "3"),
            @Parameter(name = "departmentId", description = "수정 변경할 학과 고유 ID", example = "1")
    })
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
                        "nickname": "소셜유저_a1b2c",
                        "universityName": "인제대학교",
                        "departmentName": "컴퓨터공학과",
                        "grade": 2
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (USER4002)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "USER4002",
                      "message": "선택하신 학교 또는 학과 정보가 올바르지 않거나 존재하지 않습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<ProfileResponse> updateProfile(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @PathVariable Long universityId,
            @PathVariable Long departmentId,
            @RequestBody ProfileUpdateRequest request
    );

    @Operation(summary = "알림 설정 조회", description = "키워드 알림 및 테마별 알림 수신 승인 여부를 체크합니다.")
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
                        "isAll": true,
                        "isClosing": true,
                        "isKeyword": false,
                        "scholarship": true,
                        "extracurricular": true,
                        "academic": false,
                        "employment": false
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
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
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<AlarmSettingResponse> getAlarmSettings(
            @Parameter(hidden = true) @CurrentUserId Long userId
    );

    @Operation(summary = "알림 설정 수정", description = "수신 타겟 알림 설정 제어 상태를 변경합니다.")
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
                        "isAll": true,
                        "isClosing": true,
                        "isKeyword": true,
                        "scholarship": true,
                        "extracurricular": true,
                        "academic": true,
                        "employment": false
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "실패 예시 (ALARM4002)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "ALARM4002",
                      "message": "전체 알림이 꺼진 상태(isAll=false)에서는 하위 알림 설정을 활성화할 수 없습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<AlarmSettingResponse> updateAlarmSettings(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @RequestBody AlarmUpdateRequest request
    );

    @Operation(summary = "관심 키워드 관리(조회)", description = "본인이 커스텀 지정하거나 매핑한 관심사 키워드 세트를 가져옵니다.")
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
                            "keyword": "비교과",
                            "keywordType": "RECOMMEND"
                          },
                          {
                            "keyword": "취업",
                            "keywordType": "RECOMMEND"
                          },
                          {
                            "keyword": "개발",
                            "keywordType": "CUSTOM"
                          },
                          {
                            "keyword": "어학",
                            "keywordType": "CUSTOM"
                          }
                        ],
                        "totalCount": 4
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
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
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<UserKeywordResponse>> getUserKeywords(
            @Parameter(hidden = true) @CurrentUserId Long userId
    );

    @Operation(summary = "관심 키워드 관리(수정, 삭제, 추가 일괄 처리)", description = "기존 목록을 날리고 보낸 페이로드 리스트로 키워드를 전면 동기화합니다.")
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
                      "message": "키워드 등록이 성공적으로 완료되었습니다.",
                      "result": {
                        "content": [
                          {
                            "keyword": "취업",
                            "keywordType": "RECOMMEND"
                          },
                          {
                            "keyword": "대외할동",
                            "keywordType": "RECOMMEND"
                          },
                          {
                            "keyword": "상담",
                            "keywordType": "CUSTOM"
                          },
                          {
                            "keyword": "공모전",
                            "keywordType": "CUSTOM"
                          }
                        ],
                        "totalCount": 4
                      }
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
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
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<ListResponse<UserKeywordResponse>> updateKeywords(
            @Parameter(hidden = true) @CurrentUserId Long userId,
            @RequestBody UserKeywordUpdateRequest request
    );
}
