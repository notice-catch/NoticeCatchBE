package com.noticecatch.api.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.noticecatch.api.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    // FirebaseConfig가 credentials 미설정 시 null 빈을 등록하므로, 필수 주입 대신 ObjectProvider로 지연/선택 조회한다
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    // 토큰이 만료/미등록으로 확인되면 user.pushToken을 비운다 — 호출자가 트랜잭션 안에서 호출해야 반영됨
    public void send(User user, String title, String body, Long noticeId) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null || user.getPushToken() == null || user.getPushToken().isBlank()) {
            return;
        }

        // data 페이로드로 noticeId를 실어서, 클라이언트가 알림 탭 시 해당 공지 상세로 딥링크할 수 있게 함
        Message message = Message.builder()
                .setToken(user.getPushToken())
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .putData("noticeId", String.valueOf(noticeId))
                .build();

        try {
            firebaseMessaging.send(message);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("유효하지 않은 FCM 토큰이라 초기화합니다. userId={}", user.getId());
                user.updatePushToken(null);
            } else {
                log.error("FCM 푸시 발송 실패. userId={}, code={}", user.getId(), e.getMessagingErrorCode(), e);
            }
        }
    }
}
