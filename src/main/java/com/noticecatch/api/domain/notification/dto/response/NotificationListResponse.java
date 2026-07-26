package com.noticecatch.api.domain.notification.dto.response;

import com.noticecatch.api.domain.notification.entity.Notification;
import com.noticecatch.api.domain.notification.entity.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListResponse {

    private List<NotificationItem> content;
    private int page;
    private int size;
    private boolean hasNext;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationItem {
        private Long notificationId;
        private Long noticeId;
        private NotificationType notificationType;
        private String title;
        private String message;
        private Boolean isRead;
        private LocalDateTime createdAt;

        public static NotificationItem from(Notification notification) {
            return NotificationItem.builder()
                    .notificationId(notification.getId())
                    .noticeId(notification.getNotice() != null ? notification.getNotice().getId() : null)
                    .notificationType(notification.getNotificationType())
                    .title(notification.getTitle())
                    .message(notification.getMessage())
                    .isRead(notification.getIsRead())
                    .createdAt(notification.getCreatedAt())
                    .build();
        }
    }

    public static NotificationListResponse from(Slice<Notification> notificationSlice) {
        List<NotificationItem> items = notificationSlice.getContent().stream()
                .map(NotificationItem::from)
                .toList();

        return NotificationListResponse.builder()
                .content(items)
                .page(notificationSlice.getNumber())
                .size(notificationSlice.getSize())
                .hasNext(notificationSlice.hasNext())
                .build();
    }
}