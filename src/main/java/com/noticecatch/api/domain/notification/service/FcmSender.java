package com.noticecatch.api.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.SendResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    // FCM sendEach는 배치 1건당 최대 500개 메시지까지만 허용한다
    private static final int BATCH_LIMIT = 500;

    // FirebaseConfig가 credentials 미설정 시 null 빈을 등록하므로, 필수 주입 대신 ObjectProvider로 지연/선택 조회한다
    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    // 호출자가 DB 트랜잭션 밖(커밋 이후)에서 호출한다는 전제로 설계됨 — 엔티티를 직접 건드리지 않고,
    // pushToken 초기화가 필요한(UNREGISTERED) 유저 id 목록만 반환하니 호출자가 별도 트랜잭션으로 정리해야 한다.
    public List<Long> sendBatch(List<PendingPush> pushes) {
        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging == null) {
            return List.of();
        }

        List<PendingPush> sendable = pushes.stream()
                .filter(push -> push.pushToken() != null && !push.pushToken().isBlank())
                .toList();
        if (sendable.isEmpty()) {
            return List.of();
        }

        List<Long> unregisteredUserIds = new ArrayList<>();
        for (int from = 0; from < sendable.size(); from += BATCH_LIMIT) {
            List<PendingPush> chunk = sendable.subList(from, Math.min(from + BATCH_LIMIT, sendable.size()));
            sendChunk(firebaseMessaging, chunk, unregisteredUserIds);
        }
        return unregisteredUserIds;
    }

    private void sendChunk(FirebaseMessaging firebaseMessaging, List<PendingPush> chunk, List<Long> unregisteredUserIds) {
        List<Message> messages = chunk.stream().map(this::toMessage).toList();
        try {
            BatchResponse response = firebaseMessaging.sendEach(messages);
            List<SendResponse> results = response.getResponses();
            for (int i = 0; i < results.size(); i++) {
                SendResponse result = results.get(i);
                if (result.isSuccessful()) {
                    continue;
                }
                PendingPush failed = chunk.get(i);
                FirebaseMessagingException exception = result.getException();
                if (exception != null && exception.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                    log.warn("유효하지 않은 FCM 토큰이라 초기화합니다. userId={}", failed.userId());
                    unregisteredUserIds.add(failed.userId());
                } else {
                    log.error("FCM 푸시 발송 실패. userId={}, code={}", failed.userId(),
                            exception != null ? exception.getMessagingErrorCode() : null);
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("FCM 배치 발송 실패. size={}", chunk.size(), e);
        }
    }

    private Message toMessage(PendingPush push) {
        // data 페이로드로 noticeId를 실어서, 클라이언트가 알림 탭 시 해당 공지 상세로 딥링크할 수 있게 함
        return Message.builder()
                .setToken(push.pushToken())
                .setNotification(Notification.builder()
                        .setTitle(push.title())
                        .setBody(push.body())
                        .build())
                .putData("noticeId", String.valueOf(push.noticeId()))
                .build();
    }
}
