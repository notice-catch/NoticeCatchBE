package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "🧪 Test Auth (개발/테스트 전용)", description = "소셜 통신 없이 지정한 userId로 JWT 토큰을 발급받는 테스트 API")
public interface TestAuthControllerDocs {

    @Operation(
            summary = "테스트용 JWT 토큰 직접 발급",
            description = "소셜 인가 코드/토큰 통신 없이 백엔드에서 특정 userId 기준의 AccessToken과 RefreshToken을 바로 생성해 반환합니다."
    )
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
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "nickname": "테스트유저",
                        "isNewUser": false
                      }
                    }
                    """
                            )
                    )
            )
    })
    ApiResponse<LoginResponse> testLogin(@RequestParam Long userId);
}