package com.noticecatch.api.domain.user.controller;

import com.noticecatch.api.domain.keyword.dto.request.UserKeywordUpdateRequest;
import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.domain.keyword.service.UserKeywordService;
import com.noticecatch.api.domain.user.dto.request.AlarmUpdateRequest;
import com.noticecatch.api.domain.user.dto.request.ProfileUpdateRequest;
import com.noticecatch.api.domain.user.dto.response.AlarmSettingResponse;
import com.noticecatch.api.domain.user.dto.response.MyPageProfileResponse;
import com.noticecatch.api.domain.user.dto.response.ProfileResponse;
import com.noticecatch.api.domain.user.service.UserService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UserController implements UserControllerDocs {

    private final UserService userService;
    private final UserKeywordService userKeywordService;

    @Override
    @PatchMapping("/users/department/{departmentId}")
    public ApiResponse<Void> setupOnboardingProfile(
            @CurrentUserId Long userId, @PathVariable Long departmentId,
            @RequestBody ProfileUpdateRequest request) {
        userService.updateOnboardingProfile(userId, departmentId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    @Override
    @GetMapping("/users/profile")
    public ApiResponse<MyPageProfileResponse> getProfile(@CurrentUserId Long userId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userService.getMyPage(userId));
    }

    @Override
    @PatchMapping("/users/profile/universities/{universityId}/departments/{departmentId}")
    public ApiResponse<ProfileResponse> updateProfile(
            @CurrentUserId Long userId,
            @PathVariable Long universityId,
            @PathVariable Long departmentId,
            @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                userService.updateProfile(userId, universityId, departmentId, request));
    }

    @Override
    @GetMapping("/users/alarm")
    public ApiResponse<AlarmSettingResponse> getAlarmSettings(@CurrentUserId Long userId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userService.getAlarmSettings(userId));
    }

    @Override
    @PatchMapping("/users/alarm")
    public ApiResponse<AlarmSettingResponse> updateAlarmSettings(
            @CurrentUserId Long userId,
            @RequestBody AlarmUpdateRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userService.updateAlarmSettings(userId, request));
    }

    @Override
    @GetMapping("/users/keywords")
    public ApiResponse<ListResponse<UserKeywordResponse>> getUserKeywords(@CurrentUserId Long userId) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userKeywordService.getUserKeywords(userId));
    }

    @Override
    @PutMapping("/users/keywords")
    public ApiResponse<ListResponse<UserKeywordResponse>> updateKeywords(
            @CurrentUserId Long userId,
            @RequestBody UserKeywordUpdateRequest request) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, userKeywordService.updateUserKeywords(userId, request));
    }
}
