package com.noticecatch.api.domain.auth.service;

import com.noticecatch.api.domain.auth.dto.response.OAuthUserInfo;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuthClient {

    protected final WebClient webClient;

    public OAuthUserInfo getUserInfo(String socialType, String socialToken) {
        if (socialToken == null || socialToken.isBlank()) {
            throw new ProjectException(UserErrorCode.INVALID_OAUTH_TOKEN);
        }

        if ("KAKAO".equalsIgnoreCase(socialType)) {
            return getKakaoUserInfo(socialToken);
        } else if ("GOOGLE".equalsIgnoreCase(socialType)) {
            return getGoogleUserInfo(socialToken);
        }
        throw new ProjectException(UserErrorCode.INVALID_OAUTH_PROVIDER);
    }

    // SDK Access Token을 Header(Bearer)로 전달
    private OAuthUserInfo getKakaoUserInfo(String accessToken) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri("https://kapi.kakao.com/v2/user/me")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=utf-8")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null) {
                throw new ProjectException(UserErrorCode.INVALID_OAUTH_TOKEN);
            }

            String socialId = String.valueOf(response.get("id"));

            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) response.get("kakao_account");

            String email = null;
            String nickname = null;

            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");

                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    nickname = (String) profile.get("nickname");
                }
            }

            return new OAuthUserInfo(socialId, email, nickname, "KAKAO");
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kakao UserInfo Fetch Error: {}", e.getMessage());
            throw new ProjectException(UserErrorCode.INVALID_OAUTH_TOKEN);
        }
    }

    // SDK ID Token을 Body로 전달
    private OAuthUserInfo getGoogleUserInfo(String socialToken) {
        try {
            Map<String, Object> response;

            // ID Token (JWT 형태인 경우 -> 구글 tokeninfo 엔드포인트에 Body(POST)로 전송
            if (socialToken.split("\\.").length == 3) {
                MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
                formData.add("id_token", socialToken);

                response = webClient.post()
                        .uri("https://oauth2.googleapis.com/tokeninfo")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                        .body(BodyInserters.fromFormData(formData))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
            } else {
                // 일반 Access Token일 경우
                response = webClient.get()
                        .uri("https://www.googleapis.com/oauth2/v3/userinfo")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + socialToken)
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .block();
            }

            if (response == null) {
                throw new ProjectException(UserErrorCode.INVALID_OAUTH_TOKEN);
            }

            String socialId = (String) response.get("sub");
            String email = (String) response.get("email");
            String nickname = (String) response.get("name");

            return new OAuthUserInfo(socialId, email, nickname, "GOOGLE");
        } catch (ProjectException e) {
            throw e;
        } catch (Exception e) {
            log.error("Google UserInfo Fetch Error: {}", e.getMessage());
            throw new ProjectException(UserErrorCode.INVALID_OAUTH_TOKEN);
        }
    }
}