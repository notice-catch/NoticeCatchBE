package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.request.LoginRequest;
import com.noticecatch.api.domain.auth.dto.request.TokenReissueRequest;
import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.domain.auth.dto.response.TokenReissueResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@Tag(name = "🔐 Auth", description = "인증 및 로그인 관련 API")
public interface AuthControllerDocs {
    @Operation(summary = "회원가입/로그인", description = "소셜 액세스 토큰으로 로그인 및 회원가입을 진행합니다.")
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
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "nickname": "소셜유저",
                        "isNewUser": true
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
                      "message": "유효하지 않거나 만료된 소셜 인가 토큰입니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<LoginResponse> login(@RequestBody LoginRequest request);

    @Operation(summary = "토큰 재발급", description = "Refresh Token으로 Access/Refresh Token을 재발급합니다. Refresh Token은 재발급 시마다 새 값으로 교체(rotate)됩니다.")
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
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
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
                      "message": "유효하지 않거나 만료된 리프레시 토큰입니다. 다시 로그인해 주세요.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<TokenReissueResponse> reissue(@RequestBody TokenReissueRequest request);

    @Operation(summary = "로그아웃", description = "유저 로그아웃 처리를 진행합니다.")
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
                      "message": "로그아웃이 성공적으로 완료되었습니다.",
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
                      "message": "인증 자격 증명이 유효하지 않거나 이미 만료되었습니다.",
                      "result": null
                    }
                    """
                            )
                    )
            )
    })
    com.noticecatch.api.global.apiPayload.ApiResponse<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @CurrentUserId Long userId);
}
