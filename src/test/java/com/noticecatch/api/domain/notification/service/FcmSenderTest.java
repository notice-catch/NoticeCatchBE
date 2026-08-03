package com.noticecatch.api.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.noticecatch.api.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmSenderTest {

    @Mock
    private ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    @Mock
    private FirebaseMessaging firebaseMessaging;

    private FcmSender fcmSender;

    private User userWithToken(String pushToken) {
        return User.builder().id(1L).email("a@a.com").nickname("유저").pushToken(pushToken).build();
    }

    @Test
    void Firebase가_설정안된_환경이면_조용히_아무것도_안한다() {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(null);
        User user = userWithToken("token");

        fcmSender.send(user, "제목", "내용", 1L);

        verify(firebaseMessagingProvider).getIfAvailable();
        // firebaseMessaging(null) 쪽으로는 아무 호출도 안 나가야 함 — 예외 없이 리턴되는 것 자체가 검증 포인트
    }

    @Test
    void pushToken이_없으면_발송을_시도하지_않는다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        User user = userWithToken(null);

        fcmSender.send(user, "제목", "내용", 1L);

        verify(firebaseMessaging, never()).send(any(Message.class));
    }

    @Test
    void 정상_발송시_FirebaseMessaging_send를_호출한다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        User user = userWithToken("valid-token");

        fcmSender.send(user, "제목", "내용", 1L);

        verify(firebaseMessaging).send(any(Message.class));
        assertThat(user.getPushToken()).isEqualTo("valid-token"); // 성공 시 토큰 안 건드림
    }

    @Test
    void UNREGISTERED_에러면_유저의_pushToken을_비운다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        User user = userWithToken("stale-token");

        FirebaseMessagingException exception = mockExceptionWithCode(MessagingErrorCode.UNREGISTERED);
        given(firebaseMessaging.send(any(Message.class))).willThrow(exception);

        fcmSender.send(user, "제목", "내용", 1L);

        assertThat(user.getPushToken()).isNull();
    }

    @Test
    void UNREGISTERED가_아닌_에러면_pushToken을_그대로_둔다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        User user = userWithToken("token");

        FirebaseMessagingException exception = mockExceptionWithCode(MessagingErrorCode.INTERNAL);
        given(firebaseMessaging.send(any(Message.class))).willThrow(exception);

        fcmSender.send(user, "제목", "내용", 1L);

        assertThat(user.getPushToken()).isEqualTo("token");
    }

    private FirebaseMessagingException mockExceptionWithCode(MessagingErrorCode code) {
        FirebaseMessagingException exception = org.mockito.Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(code);
        return exception;
    }
}
