package com.noticecatch.api.domain.user.entity;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.university.entity.University;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@DynamicInsert
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = true)
    private Department department;

    // 온보딩 중 대학만 먼저 선택하고 학과는 아직 안 고른 상태를 표현하기 위한 필드.
    // 학과가 정해지면 department.getUniversity()가 진짜 소속 대학이 되고, 이 필드는 더 이상 쓰이지 않음.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = true)
    private University university;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String nickname;

    @Column(name = "social_provider", length = 255)
    private String socialProvider;

    @Column(name = "social_id", length = 255)
    private String socialId;

    private Integer grade; // 학년

    @Column(name = "all_notification")
    @ColumnDefault("true")
    private Boolean allNotification;

    @Column(name = "closing_notification")
    @ColumnDefault("true")
    private Boolean closingNotification;

    @Column(name = "keyword_notification")
    @ColumnDefault("true")
    private Boolean keywordNotification;

    @ColumnDefault("true")
    private Boolean scholarship;

    @Column(name = "extracurricular")
    @ColumnDefault("true")
    private Boolean extracurricular;

    @ColumnDefault("true")
    private Boolean academic;

    @ColumnDefault("true")
    private Boolean employment;

    @Column(name = "push_token", length = 500)
    private String pushToken; // FCM 기기 토큰 스트링

    @Column(length = 255)
    private String role;

    @Column(length = 255)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.role == null) this.role = "USER";
        if (this.status == null) this.status = "ACTIVE";
    }

    // FCM 디바이스 토큰 갱신 (로그인/앱 재설치 시 호출)
    public void updatePushToken(String newPushToken) {
        this.pushToken = newPushToken;
    }

    //전체 알람 설정
    public void toggleAllNotification(boolean status) {
        this.allNotification = status;
    }

    // 알림 설정 전체 갱신 (부분 수정은 서비스 계층에서 기존 값과 병합 후 호출)
    public void updateAlarmSettings(boolean allNotification, boolean closingNotification, boolean keywordNotification,
                                     boolean scholarship, boolean extracurricular, boolean academic, boolean employment) {
        this.allNotification = allNotification;
        this.closingNotification = closingNotification;
        this.keywordNotification = keywordNotification;
        this.scholarship = scholarship;
        this.extracurricular = extracurricular;
        this.academic = academic;
        this.employment = employment;
    }

    //키워드별 알람 설정
    public void updateCategoryNotifications(boolean scholarship, boolean extracurricular, boolean academic, boolean employment) {
        this.scholarship = scholarship;
        this.extracurricular = extracurricular;
        this.academic = academic;
        this.employment = employment;
    }

    public void changeDepartment(Department department) {
        this.department = department;
    }

    public void changeUniversity(University university) {
        this.university = university;
    }

    public void updateProfile(String nickname, Integer grade) {
        this.nickname = nickname;
        this.grade = grade;
    }

    public void withdraw() {
        this.status = "WITHDRAWN";
    }
}
