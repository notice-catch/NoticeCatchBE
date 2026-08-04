package com.noticecatch.api.domain.notification.service;

import com.noticecatch.api.domain.notification.dto.request.DeviceTokenRequest;
import com.noticecatch.api.domain.notification.dto.response.NotificationListResponse;
import com.noticecatch.api.domain.notification.entity.Notification;
import com.noticecatch.api.domain.notification.entity.NotificationType;
import com.noticecatch.api.domain.notification.exception.NotificationErrorCode;
import com.noticecatch.api.domain.notification.repository.NotificationRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationBatchService notificationBatchService;
    @Mock
    private FcmSender fcmSender;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(notificationRepository, userRepository, notificationBatchService, fcmSender);
    }

    private User user(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").build();
    }

    private DeviceTokenRequest deviceTokenRequest(String pushToken) {
        DeviceTokenRequest request = new DeviceTokenRequest();
        ReflectionTestUtils.setField(request, "pushToken", pushToken);
        return request;
    }

    @Test
    void 디바이스토큰이_비어있으면_PUSH_TOKEN_INVALID를_던진다() {
        assertThatThrownBy(() -> notificationService.registerDeviceToken(1L, deviceTokenRequest(" ")))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(NotificationErrorCode.PUSH_TOKEN_INVALID);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void 존재하지_않는_유저면_디바이스토큰_등록시_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.registerDeviceToken(999L, deviceTokenRequest("token")))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 정상_요청이면_디바이스토큰을_갱신한다() {
        User user = user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        notificationService.registerDeviceToken(1L, deviceTokenRequest("new-token"));

        assertThat(user.getPushToken()).isEqualTo("new-token");
    }

    @Test
    void 알림함_목록을_페이지네이션으로_조회한다() {
        User user = user(1L);
        Notification notification = Notification.builder()
                .id(1L).user(user).notificationType(NotificationType.KEYWORD)
                .title("제목").message("메시지").isRead(false).build();
        Slice<Notification> slice = new SliceImpl<>(List.of(notification));
        given(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(PageRequest.class))).willReturn(slice);

        NotificationListResponse response = notificationService.getNotifications(1L, 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getTitle()).isEqualTo("제목");
    }

    @Test
    void 알림_전체읽음_처리를_위임한다() {
        notificationService.readAllNotifications(1L);

        verify(notificationRepository).markAllAsReadByUserId(1L);
    }

    @Test
    void 발송할_알림이_없으면_FCM을_호출하지_않는다() {
        given(notificationBatchService.persistPendingNoticeNotifications()).willReturn(List.of());

        notificationService.notifyPendingNotices();

        verify(fcmSender, never()).sendBatch(any());
        verify(notificationBatchService, never()).clearPushTokens(any());
    }

    @Test
    void 발송_실패한_토큰이_없으면_토큰정리를_호출하지_않는다() {
        List<PendingPush> pushes = List.of(new PendingPush(1L, "token-1", "제목", "내용", 10L));
        given(notificationBatchService.persistPendingNoticeNotifications()).willReturn(pushes);
        given(fcmSender.sendBatch(pushes)).willReturn(List.of());

        notificationService.notifyPendingNotices();

        verify(fcmSender).sendBatch(pushes);
        verify(notificationBatchService, never()).clearPushTokens(any());
    }

    @Test
    void UNREGISTERED_토큰이_있으면_해당_유저의_토큰을_정리한다() {
        List<PendingPush> pushes = List.of(
                new PendingPush(1L, "token-1", "제목", "내용", 10L),
                new PendingPush(2L, "stale-token", "제목", "내용", 10L));
        given(notificationBatchService.persistPendingNoticeNotifications()).willReturn(pushes);
        given(fcmSender.sendBatch(pushes)).willReturn(List.of(2L));

        notificationService.notifyPendingNotices();

        verify(notificationBatchService).clearPushTokens(List.of(2L));
    }

    @Test
    void 마감임박_알림도_동일하게_배치_저장_후_발송한다() {
        List<PendingPush> pushes = List.of(new PendingPush(1L, "token-1", "제목", "내용", 10L));
        given(notificationBatchService.persistClosingSoonNotifications()).willReturn(pushes);
        given(fcmSender.sendBatch(pushes)).willReturn(List.of());

        notificationService.notifyClosingSoonNotices();

        verify(notificationBatchService).persistClosingSoonNotifications();
        verify(fcmSender).sendBatch(pushes);
        verify(notificationBatchService, never()).clearPushTokens(any());
    }
}
