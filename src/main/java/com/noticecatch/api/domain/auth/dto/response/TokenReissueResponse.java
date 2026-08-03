package com.noticecatch.api.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenReissueResponse {
    private String accessToken;  // 새로 발급된 Access Token
    private String refreshToken; // 새로 발급된 Refresh Token (재발급 시마다 회전)
}
