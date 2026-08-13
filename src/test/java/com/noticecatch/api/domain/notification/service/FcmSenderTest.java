package com.noticecatch.api.domain.notification.service;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmSenderTest {

    @Mock
    private ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;
    @Mock
    private FirebaseMessaging firebaseMessaging;

    private FcmSender fcmSender;

    private PendingPush push(Long userId, String pushToken) {
        return new PendingPush(userId, pushToken, "제목", "내용", 1L);
    }

    private SendResponse success() {
        SendResponse response = org.mockito.Mockito.mock(SendResponse.class);
        given(response.isSuccessful()).willReturn(true);
        return response;
    }

    private SendResponse failure(MessagingErrorCode code) {
        SendResponse response = org.mockito.Mockito.mock(SendResponse.class);
        FirebaseMessagingException exception = org.mockito.Mockito.mock(FirebaseMessagingException.class);
        given(exception.getMessagingErrorCode()).willReturn(code);
        given(response.isSuccessful()).willReturn(false);
        given(response.getException()).willReturn(exception);
        return response;
    }

    private BatchResponse batchResponse(List<SendResponse> responses) {
        BatchResponse batchResponse = org.mockito.Mockito.mock(BatchResponse.class);
        given(batchResponse.getResponses()).willReturn(responses);
        return batchResponse;
    }

    @Test
    void Firebase가_설정안된_환경이면_조용히_빈_목록을_반환한다() {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(null);

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, "token")));

        assertThat(failed).isEmpty();
    }

    @Test
    void 유효한_pushToken이_하나도_없으면_발송을_시도하지_않는다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, null), push(2L, " ")));

        assertThat(failed).isEmpty();
        verify(firebaseMessaging, never()).sendEach(anyList());
    }

    @Test
    void 정상_발송시_실패목록이_비어있다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        BatchResponse response = batchResponse(List.of(success()));
        given(firebaseMessaging.sendEach(anyList())).willReturn(response);

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, "valid-token")));

        assertThat(failed).isEmpty();
        verify(firebaseMessaging).sendEach(anyList());
    }

    @Test
    void UNREGISTERED_에러면_해당_유저_id를_실패목록으로_반환한다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        BatchResponse response = batchResponse(List.of(failure(MessagingErrorCode.UNREGISTERED)));
        given(firebaseMessaging.sendEach(anyList())).willReturn(response);

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, "stale-token")));

        assertThat(failed).containsExactly(1L);
    }

    @Test
    void UNREGISTERED가_아닌_에러는_실패목록에_포함하지_않는다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        BatchResponse response = batchResponse(List.of(failure(MessagingErrorCode.INTERNAL)));
        given(firebaseMessaging.sendEach(anyList())).willReturn(response);

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, "token")));

        assertThat(failed).isEmpty();
    }

    @Test
    void 여러명_중_일부만_UNREGISTERED면_해당_유저만_반환한다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        BatchResponse response = batchResponse(List.of(success(), failure(MessagingErrorCode.UNREGISTERED), success()));
        given(firebaseMessaging.sendEach(anyList())).willReturn(response);

        List<Long> failed = fcmSender.sendBatch(List.of(
                push(1L, "token-1"), push(2L, "token-2"), push(3L, "token-3")));

        assertThat(failed).containsExactly(2L);
    }

    @Test
    void 배치발송_전체가_예외를_던져도_다음_청크는_영향받지_않는다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);
        given(firebaseMessaging.sendEach(anyList()))
                .willThrow(org.mockito.Mockito.mock(FirebaseMessagingException.class));

        List<Long> failed = fcmSender.sendBatch(List.of(push(1L, "token")));

        assertThat(failed).isEmpty();
    }

    @Test
    void 배치한도_500건을_초과하면_나눠서_발송한다() throws Exception {
        fcmSender = new FcmSender(firebaseMessagingProvider);
        given(firebaseMessagingProvider.getIfAvailable()).willReturn(firebaseMessaging);

        List<PendingPush> pushes = IntStream.range(0, 501)
                .mapToObj(i -> push((long) i, "token-" + i))
                .toList();

        given(firebaseMessaging.sendEach(anyList())).willAnswer(invocation -> {
            List<Message> messages = invocation.getArgument(0);
            List<SendResponse> responses = new ArrayList<>();
            messages.forEach(m -> responses.add(success()));
            return batchResponse(responses);
        });

        fcmSender.sendBatch(pushes);

        verify(firebaseMessaging, times(2)).sendEach(anyList());
    }
}
