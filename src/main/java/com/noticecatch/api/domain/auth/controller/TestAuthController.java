package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.jwt.JwtProvider;
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

    private final JwtProvider jwtProvider; // @Autowired 대신 생성자 주입 추천

    @PostMapping("/test-login")
    public ApiResponse<LoginResponse> testLogin(@RequestParam Long userId) {
        // 소셜 통신 없이 지정한 userId로 백엔드 JWT 발급
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nickname("테스트유저")
                .isNewUser(false)
                .build();

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
