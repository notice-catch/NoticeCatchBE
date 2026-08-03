package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.jwt.JwtProvider;
import com.noticecatch.api.global.redis.RedisService;
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
    private final RedisService redisService;

    @PostMapping("/test-login")
    public ApiResponse<LoginResponse> testLogin(@RequestParam Long userId) {
        // 소셜 통신 없이 지정한 userId로 백엔드 JWT 발급
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        // 실제 로그인과 동일하게 Redis에 Refresh Token 저장 — 안 하면 /auth/reissue, /auth/logout이 테스트 로그인 유저에 대해 항상 실패함
        redisService.saveRefreshToken(userId, refreshToken, jwtProvider.getRefreshTokenExpirationMillis());

        LoginResponse response = LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .nickname("테스트유저")
                .isNewUser(false)
                .build();

        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }
}
