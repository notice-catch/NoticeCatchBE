package com.noticecatch.api.domain.notice.entity;

import com.noticecatch.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserNotice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id", nullable = false)
    private Notice notice;

    @Column(name = "is_scrapped")
    private Boolean isScrapped;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isScrapped == null) this.isScrapped = false;
        if (this.isRead == null) this.isRead = false;
    }

    public static UserNotice create(User user, Notice notice) {
        return UserNotice.builder()
                .user(user)
                .notice(notice)
                .isScrapped(false)
                .isRead(false)
                .build();
    }

    public void toggleScrap() {
        this.isScrapped = !this.isScrapped;
    }

    public void markAsRead() {
        this.isRead = true;
    }
}