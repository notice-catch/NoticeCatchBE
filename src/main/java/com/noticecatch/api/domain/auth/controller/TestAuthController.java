package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.domain.auth.dto.response.TokenReissueResponse;
import com.noticecatch.api.domain.auth.service.AuthService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class TestAuthController implements TestAuthControllerDocs {

    private final AuthService authService;

    @PostMapping("/test-login")
    public ApiResponse<LoginResponse> testLogin(@RequestParam Long userId) {
        // 소셜 통신 없이 지정한 userId로, 실제 로그인과 동일한 발급 로직(JWT 생성 + Redis Refresh Token 저장)을 재사용
        TokenReissueResponse tokens = authService.issueTokens(userId);

        LoginResponse response = LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .nickname("테스트유저")
                .isNewUser(false)
                .build();

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
