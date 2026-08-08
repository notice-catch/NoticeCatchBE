package com.noticecatch.api.domain.auth.service;

import com.noticecatch.api.domain.auth.dto.request.LoginRequest;
import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.domain.auth.dto.response.OAuthUserInfo;
import com.noticecatch.api.domain.auth.dto.response.TokenReissueResponse;
import com.noticecatch.api.domain.auth.exception.AuthErrorCode;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.jwt.JwtProvider;
import com.noticecatch.api.global.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthClient oAuthClient;
    private final JwtProvider jwtProvider;
    private final RedisService redisService;

    public LoginResponse socialLogin(LoginRequest request) {
        // 1. 프론트엔드에서 전달받은 토큰으로 유저 정보 조회
        OAuthUserInfo userInfo = oAuthClient.getUserInfo(request.getSocialType(), request.getAuthorizationCode());

        AtomicBoolean isNewUser = new AtomicBoolean(false);

        // 2. 이메일 기반 기존 유저 조회 또는 신규 회원가입
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    isNewUser.set(true);
                    return registerNewUser(userInfo);
                });

        // 3. JWT 토큰 발급 및 Redis에 Refresh Token 저장
        TokenReissueResponse tokens = issueTokens(user.getId());

        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .nickname(user.getNickname())
                .isNewUser(isNewUser.get())
                .build();
    }

    private User registerNewUser(OAuthUserInfo userInfo) {
        User newUser = User.builder()
                .email(userInfo.getEmail())
                .nickname(userInfo.getNickname())
                .socialProvider(userInfo.getSocialType())
                .socialId(userInfo.getSocialId())
                .build();

        return userRepository.save(newUser);
    }

    public TokenReissueResponse reissueToken(String refreshToken) {
        // 1. 서명/만료 검증
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new ProjectException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // 2. Redis에 저장된 토큰과 일치하는지 대조 (로그아웃되었거나, 이미 재발급되어 회전된 토큰 재사용 방지)
        String savedRefreshToken = redisService.getRefreshToken(userId);
        if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new ProjectException(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. Access/Refresh Token 재발급 및 Redis 갱신 (Refresh Token Rotation)
        return issueTokens(userId);
    }

    // 소셜 로그인/테스트 로그인/재발급이 공통으로 쓰는 발급 로직: JWT 생성 + Redis에 Refresh Token 저장
    public TokenReissueResponse issueTokens(Long userId) {
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);
        redisService.saveRefreshToken(userId, refreshToken, jwtProvider.getRefreshTokenExpirationMillis());

        return TokenReissueResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * @param accessToken 순수 Access Token 값 ("Bearer " 제외)
     * @param userId 인증된 사용자 ID
     */
    public void logout(String accessToken, Long userId) {
        // 1. 유저 존재 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        // 2. Redis에서 해당 유저의 Refresh Token 삭제
        redisService.deleteRefreshToken(user.getId());

        // 3. Access Token의 남은 만료 시간을 구해서 Redis 블랙리스트 등록
        long remainingExpirationMillis = jwtProvider.getExpirationMillis(accessToken);
        redisService.setBlackList(accessToken, remainingExpirationMillis);
    }
}