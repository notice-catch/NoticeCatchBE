package com.noticecatch.api.domain.notification.service;

import com.noticecatch.api.domain.notification.dto.request.DeviceTokenRequest;
import com.noticecatch.api.domain.notification.dto.response.NotificationListResponse;
import com.noticecatch.api.domain.notification.exception.NotificationErrorCode;
import com.noticecatch.api.domain.notification.repository.NotificationRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        if (request == null || request.getPushToken() == null || request.getPushToken().isBlank()) {
            throw new ProjectException(NotificationErrorCode.PUSH_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        user.updatePushToken(request.getPushToken());
    }

    public NotificationListResponse getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var notificationSlice = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return NotificationListResponse.from(notificationSlice);
    }

    @Transactional
    public void readAllNotifications(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}