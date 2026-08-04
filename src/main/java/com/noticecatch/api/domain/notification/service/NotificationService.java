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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationBatchService notificationBatchService;
    private final FcmSender fcmSender;

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

    // 크롤러가 DB에 직접 넣는 신규 공지를 5분마다 스캔해 키워드/카테고리 알림을 발송한다.
    // DB 저장(NotificationBatchService, 자체 트랜잭션)과 FCM 발송(네트워크 호출)을 분리해서
    // 이 메서드 자체는 트랜잭션 없이 실행되고, 푸시 발송 시간만큼 DB 커넥션이 잡혀있지 않게 한다.
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void notifyPendingNotices() {
        dispatch(notificationBatchService.persistPendingNoticeNotifications());
    }

    // 매일 오전 9시 — 스크랩한 공지 중 마감 D-3 이내로 들어온 것에 대해 유저별로 한 번만 발송
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyClosingSoonNotices() {
        dispatch(notificationBatchService.persistClosingSoonNotifications());
    }

    private void dispatch(List<PendingPush> pushes) {
        if (pushes.isEmpty()) {
            return;
        }
        List<Long> unregisteredUserIds = fcmSender.sendBatch(pushes);
        if (!unregisteredUserIds.isEmpty()) {
            notificationBatchService.clearPushTokens(unregisteredUserIds);
        }
    }
}
