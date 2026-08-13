package com.noticecatch.api.domain.notice.entity;

import com.noticecatch.api.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_notices",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_notices_user_id_notice_id", columnNames = {"user_id", "notice_id"}))
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

    // 마감임박(D-day) 알림을 이미 보냈는지 — 스크랩한 유저별로 한 번만 발송하기 위한 플래그
    @Column(name = "closing_notified")
    private Boolean closingNotified;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.isScrapped == null) this.isScrapped = false;
        if (this.isRead == null) this.isRead = false;
        if (this.closingNotified == null) this.closingNotified = false;
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

    public void markClosingNotified() {
        this.closingNotified = true;
    }
}