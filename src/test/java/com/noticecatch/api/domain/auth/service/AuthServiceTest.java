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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OAuthClient oAuthClient;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private RedisService redisService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, oAuthClient, jwtProvider, redisService);
    }

    private User existingUser(Long id, String email) {
        return User.builder().id(id).email(email).nickname("기존유저").build();
    }

    @Test
    void 기존_회원이면_새로_가입시키지_않고_로그인만_한다() {
        LoginRequest request = new LoginRequest("auth-code", "KAKAO");
        OAuthUserInfo userInfo = new OAuthUserInfo("social-1", "existing@example.com", "카카오닉네임", "KAKAO");
        User user = existingUser(1L, "existing@example.com");

        given(oAuthClient.getUserInfo("KAKAO", "auth-code")).willReturn(userInfo);
        given(userRepository.findByEmail("existing@example.com")).willReturn(Optional.of(user));
        given(jwtProvider.createAccessToken(1L)).willReturn("access-token");
        given(jwtProvider.createRefreshToken(1L)).willReturn("refresh-token");
        given(jwtProvider.getRefreshTokenExpirationMillis()).willReturn(1_209_600_000L);

        LoginResponse response = authService.socialLogin(request);

        assertThat(response.isNewUser()).isFalse();
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getNickname()).isEqualTo("기존유저");
        verify(userRepository, never()).save(any());
        verify(redisService).saveRefreshToken(1L, "refresh-token", 1_209_600_000L);
    }

    @Test
    void 신규_회원이면_가입시키고_isNewUser_true를_반환한다() {
        LoginRequest request = new LoginRequest("auth-code", "GOOGLE");
        OAuthUserInfo userInfo = new OAuthUserInfo("social-2", "new@example.com", "새유저", "GOOGLE");
        User savedUser = User.builder().id(2L).email("new@example.com").nickname("새유저").build();

        given(oAuthClient.getUserInfo("GOOGLE", "auth-code")).willReturn(userInfo);
        given(userRepository.findByEmail("new@example.com")).willReturn(Optional.empty());
        given(userRepository.save(any(User.class))).willReturn(savedUser);
        given(jwtProvider.createAccessToken(2L)).willReturn("access-token");
        given(jwtProvider.createRefreshToken(2L)).willReturn("refresh-token");
        given(jwtProvider.getRefreshTokenExpirationMillis()).willReturn(1_209_600_000L);

        LoginResponse response = authService.socialLogin(request);

        assertThat(response.isNewUser()).isTrue();
        verify(userRepository).save(any(User.class));
    }

    @Test
    void 유효한_리프레시토큰이고_Redis값과_일치하면_토큰을_회전해서_재발급한다() {
        given(jwtProvider.validateToken("old-refresh")).willReturn(true);
        given(jwtProvider.getUserId("old-refresh")).willReturn(1L);
        given(redisService.getRefreshToken(1L)).willReturn("old-refresh");
        given(jwtProvider.createAccessToken(1L)).willReturn("new-access");
        given(jwtProvider.createRefreshToken(1L)).willReturn("new-refresh");
        given(jwtProvider.getRefreshTokenExpirationMillis()).willReturn(1_209_600_000L);

        TokenReissueResponse response = authService.reissueToken("old-refresh");

        assertThat(response.getAccessToken()).isEqualTo("new-access");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh");
        verify(redisService).saveRefreshToken(1L, "new-refresh", 1_209_600_000L);
    }

    @Test
    void 서명이_무효한_리프레시토큰이면_AUTH401을_던진다() {
        given(jwtProvider.validateToken("invalid")).willReturn(false);

        assertThatThrownBy(() -> authService.reissueToken("invalid"))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

        verify(redisService, never()).getRefreshToken(any());
    }

    @Test
    void 서명은_유효해도_Redis에_저장된_값과_다르면_재사용으로_간주해_거부한다() {
        given(jwtProvider.validateToken("stolen-token")).willReturn(true);
        given(jwtProvider.getUserId("stolen-token")).willReturn(1L);
        given(redisService.getRefreshToken(1L)).willReturn("the-real-current-token"); // 이미 회전되어 다른 값

        assertThatThrownBy(() -> authService.reissueToken("stolen-token"))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);

        verify(jwtProvider, never()).createAccessToken(any());
    }

    @Test
    void Redis에_토큰이_아예_없으면_로그아웃된_것으로_보고_거부한다() {
        given(jwtProvider.validateToken("token")).willReturn(true);
        given(jwtProvider.getUserId("token")).willReturn(1L);
        given(redisService.getRefreshToken(1L)).willReturn(null);

        assertThatThrownBy(() -> authService.reissueToken("token"))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    void 로그아웃시_리프레시토큰_삭제와_액세스토큰_블랙리스트_등록을_한다() {
        User user = existingUser(1L, "a@a.com");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(jwtProvider.getExpirationMillis("access-token")).willReturn(3_600_000L);

        authService.logout("access-token", 1L);

        verify(redisService).deleteRefreshToken(1L);
        verify(redisService).setBlackList("access-token", 3_600_000L);
    }

    @Test
    void 로그아웃시_존재하지_않는_유저면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.logout("access-token", 999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(redisService, never()).deleteRefreshToken(any());
    }
}
