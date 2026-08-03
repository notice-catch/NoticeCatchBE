package com.noticecatch.api.domain.notice.entity;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.university.entity.University;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDateTime;

@Entity
@Table(name = "notices")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false, length = 255)
    private String title;

    @Lob
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "origin_url", length = 255)
    private String originUrl;

    @Column(name = "has_attachments")
    private Boolean hasAttachments; // 첨부파일 유무

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "deadline_at")
    private LocalDateTime deadlineAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 크롤러가 DB에 직접 insert하므로 DB 컬럼 기본값(false)에 의존 — 키워드 알림 배치가 이 값으로 미처리 공지를 찾음
    @Column(name = "notified")
    @ColumnDefault("false")
    private Boolean notified;

    @OneToOne(mappedBy = "notice", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private NoticeSummary noticeSummary;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.hasAttachments == null) this.hasAttachments = false; // 기본값 false 세팅
        if (this.notified == null) this.notified = false;
    }

    public void markNotified() {
        this.notified = true;
    }

    public void setNoticeSummary(NoticeSummary noticeSummary) {
        this.noticeSummary = noticeSummary;
        if (noticeSummary != null && noticeSummary.getNotice() != this) {
            noticeSummary.setNotice(this);
        }
    }

    public boolean isExpired() {
        if (this.deadlineAt == null) return false;
        return LocalDateTime.now().isAfter(this.deadlineAt);
    }

    // 출처 표시명 — 학과 공지면 학과명, 아니면 대학 전체 공지로 간주해 대학명, 둘 다 없으면 기본값
    public String getSourceName() {
        if (this.department != null) {
            return this.department.getName();
        }
        if (this.university != null) {
            return this.university.getName();
        }
        return "대학 본부";
    }
}
