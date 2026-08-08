package com.noticecatch.api.domain.notification.service;

// FCM 발송에 필요한 최소 정보만 담은 값 객체.
// 트랜잭션 커밋 후(엔티티가 detach된 뒤) 발송하기 위해 엔티티 대신 이 값을 주고받는다.
public record PendingPush(Long userId, String pushToken, String title, String body, Long noticeId) {
}
