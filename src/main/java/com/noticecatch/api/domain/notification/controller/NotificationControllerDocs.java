package com.noticecatch.api.domain.notification.controller;

import com.noticecatch.api.domain.notification.dto.request.DeviceTokenRequest;
import com.noticecatch.api.domain.notification.dto.response.NotificationListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "🔔 Notification", description = "기기 토큰 등록 및 알림 수신 히스토리 API")
public interface NotificationControllerDocs {
    @Operation(summary = "유저 기기 토큰 저장", description = "FCM 알림 타겟팅을 위해 모바일 디바이스 푸시 토큰을 수집합니다.")
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
                      "message": "푸시 토큰이 성공적으로 등록되었습니다.",
                      "result": null
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
                                    name = "실패 예시 (PUSH4001)",
                                    value = """
                    {
                      "isSuccess": false,
                      "code": "PUSH4001",
                      "message": "유효하지 않은 디바이스 토큰 형식입니다.",
                      "result": null
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
    com.noticecatch.api.global.apiPayload.ApiResponse<Void> saveDeviceToken(
            @Parameter(hidden = true) Long userId,
            @RequestBody DeviceTokenRequest request
    );

    @Operation(summary = "알림함 목록 조회", description = "유저에게 전송되었던 알림 리스트를 최신순 페이징하여 조회합니다.")
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
                            "notificationId": 1204,
                            "noticeId": 542,
                            "notificationType": "CLOSING",
                            "title": "📌 곧 마감되는 공지가 있어요",
                            "message": "[2026학년도 2학기 수강신청 기간 안내] D-3 · 2026-07-04 까지",
                            "isRead": false,
                            "createdAt": "2026-07-02T09:00:00"
                          },
                          {
                            "notificationId": 198,
                            "noticeId": 11,
                            "notificationType": "KEYWORD",
                            "title": "🏷️ 내 키워드와 관련된 공지가 등록되었어요",
                            "message": "[산학협력단] 2026 대학생 IT 창업 아이디어 공모전 개최",
                            "isRead": true,
                            "createdAt": "2026-07-01T15:30:00"
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
    com.noticecatch.api.global.apiPayload.ApiResponse<NotificationListResponse> getNotifications(
            @Parameter(hidden = true) Long userId,
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size
    );

    @Operation(summary = "알림함 전체 읽기", description = "미확인 상태의 전송 알림 리스트를 전부 읽음 일괄 처리합니다.")
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
                      "result": null
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
    com.noticecatch.api.global.apiPayload.ApiResponse<Void> readAllNotifications(
            @Parameter(hidden = true) Long userId
    );
}