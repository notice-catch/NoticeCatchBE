package com.noticecatch.api.domain.notification.controller;

import com.noticecatch.api.domain.notification.dto.request.DeviceTokenRequest;
import com.noticecatch.api.domain.notification.dto.response.NotificationListResponse;
import com.noticecatch.api.domain.notification.service.NotificationService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class NotificationController implements NotificationControllerDocs {

    private final NotificationService notificationService;

    @Override
    @PostMapping("/users/device")
    public ApiResponse<Void> saveDeviceToken(
            @CurrentUserId Long userId,
            @RequestBody DeviceTokenRequest request
    ) {
        notificationService.registerDeviceToken(userId, request);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }

    @Override
    @GetMapping("/notifications")
    public ApiResponse<NotificationListResponse> getNotifications(
            @CurrentUserId Long userId,
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        NotificationListResponse response = notificationService.getNotifications(userId, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PatchMapping("/notifications/read")
    public ApiResponse<Void> readAllNotifications(
            @CurrentUserId Long userId
    ) {
        notificationService.readAllNotifications(userId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }
}