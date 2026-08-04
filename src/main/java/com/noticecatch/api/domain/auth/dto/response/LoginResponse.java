package com.noticecatch.api.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;  // JWT Access Token
    private String refreshToken; // JWT Refresh Token
    private String nickname;     // 유저 닉네임

    @JsonProperty("isNewUser")
    private boolean isNewUser;   // 신규 회원 여부
}
