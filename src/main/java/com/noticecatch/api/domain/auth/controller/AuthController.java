package com.noticecatch.api.domain.auth.controller;

import com.noticecatch.api.domain.auth.dto.request.LoginRequest;
import com.noticecatch.api.domain.auth.dto.response.LoginResponse;
import com.noticecatch.api.domain.auth.service.AuthService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    @Override
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, authService.socialLogin(request));
    }

    @Override
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String bearerToken,
            @CurrentUserId Long userId
    ) {
        String accessToken = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7)
                : bearerToken;

        authService.logout(accessToken, userId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }
}
