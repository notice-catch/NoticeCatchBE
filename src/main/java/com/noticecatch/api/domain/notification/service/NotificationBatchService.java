package com.noticecatch.api.domain.notification.service;

import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.notification.entity.Notification;
import com.noticecatch.api.domain.notification.entity.NotificationType;
import com.noticecatch.api.domain.notification.repository.NotificationRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 알림 매칭 계산 + DB 저장까지만 이 클래스의 트랜잭션 안에서 끝내고, 발송에 필요한 값(PendingPush)만 반환한다.
// 실제 FCM 발송(외부 네트워크 호출)은 트랜잭션 밖(NotificationService)에서 이루어지므로,
// 여기서 DB 커넥션을 붙잡고 있는 시간이 네트워크 왕복 시간에 영향받지 않는다.
@Service
@RequiredArgsConstructor
public class NotificationBatchService {

    private static final int CLOSING_SOON_THRESHOLD_DAYS = 3;
    private static final DateTimeFormatter DEADLINE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserNoticeRepository userNoticeRepository;

    @Transactional
    public List<PendingPush> persistPendingNoticeNotifications() {
        List<Notice> pendingNotices = noticeRepository.findByNotifiedFalse();
        List<PendingPush> pushes = new ArrayList<>();
        for (Notice notice : pendingNotices) {
            pushes.addAll(persistMatchingNotifications(notice));
            notice.markNotified();
        }
        return pushes;
    }

    // 후보 유저 조회 1번 + 키워드 일괄 조회 1번으로 키워드/카테고리 매칭을 함께 처리한다
    private List<PendingPush> persistMatchingNotifications(Notice notice) {
        Long universityId = notice.getUniversity().getId();
        List<User> candidates = userRepository.findByDepartment_University_IdAndAllNotificationTrue(universityId).stream()
                .filter(user -> matchesDepartment(user, notice))
                .toList();
        if (candidates.isEmpty()) {
            return List.of();
        }

        Map<Long, List<UserKeyword>> keywordsByUserId = userKeywordRepository.findAllByUserIn(candidates).stream()
                .collect(Collectors.groupingBy(userKeyword -> userKeyword.getUser().getId()));
        String categoryName = notice.getCategory().getName();

        List<Notification> notifications = new ArrayList<>();
        for (User user : candidates) {
            if (Boolean.TRUE.equals(user.getKeywordNotification())) {
                String matchedKeyword = findMatchedKeyword(notice, keywordsByUserId.getOrDefault(user.getId(), List.of()));
                if (matchedKeyword != null) {
                    // 알림함 예시(docs/API_SPEC.md)와 동일한 고정 문구
                    notifications.add(buildNotification(user, notice, NotificationType.KEYWORD,
                            "🏷️ 내 키워드와 관련된 공지가 등록되었어요", notice.getTitle()));
                }
            }

            if (matchesCategoryPreference(user, categoryName)) {
                notifications.add(buildNotification(user, notice, NotificationType.CATEGORY,
                        "📢 관심 카테고리에 새 공지가 등록되었어요", notice.getTitle()));
            }
        }

        return saveAndToPushes(notifications);
    }

    // 마이페이지 알림 설정(scholarship/academic/employment/extracurricular)과 공지 카테고리명 매핑
    private boolean matchesCategoryPreference(User user, String categoryName) {
        return switch (categoryName) {
            case "장학" -> Boolean.TRUE.equals(user.getScholarship());
            case "학사" -> Boolean.TRUE.equals(user.getAcademic());
            case "취업" -> Boolean.TRUE.equals(user.getEmployment());
            case "비교과" -> Boolean.TRUE.equals(user.getExtracurricular());
            default -> false;
        };
    }

    // 학과 공지(department != null)는 해당 학과 유저만, 전체 공지(department == null)는 대학 소속이면 전부 대상
    private boolean matchesDepartment(User user, Notice notice) {
        if (notice.getDepartment() == null) {
            return true;
        }
        return user.getDepartment() != null
                && user.getDepartment().getId().equals(notice.getDepartment().getId());
    }

    private String findMatchedKeyword(Notice notice, List<UserKeyword> keywords) {
        for (UserKeyword userKeyword : keywords) {
            String keyword = userKeyword.getKeyword();
            boolean inTitle = notice.getTitle().contains(keyword);
            boolean inContent = notice.getContent() != null && notice.getContent().contains(keyword);
            if (inTitle || inContent) {
                return keyword;
            }
        }
        return null;
    }

    // 매일 오전 9시 — 스크랩한 공지 중 마감 D-3 이내로 들어온 것에 대해 유저별로 한 번만 발송
    @Transactional
    public List<PendingPush> persistClosingSoonNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(CLOSING_SOON_THRESHOLD_DAYS);

        List<UserNotice> candidates = userNoticeRepository.findClosingSoonCandidates(now, threshold);
        List<Notification> notifications = new ArrayList<>();
        for (UserNotice userNotice : candidates) {
            Notice notice = userNotice.getNotice();
            long daysLeft = ChronoUnit.DAYS.between(now.toLocalDate(), notice.getDeadlineAt().toLocalDate());
            String deadlineDate = notice.getDeadlineAt().format(DEADLINE_DATE_FORMAT);

            // 알림함 예시(docs/API_SPEC.md)와 동일한 고정 문구/포맷
            notifications.add(buildNotification(userNotice.getUser(), notice, NotificationType.CLOSING,
                    "📌 곧 마감되는 공지가 있어요",
                    "[" + notice.getTitle() + "] D-" + daysLeft + " · " + deadlineDate + " 까지"));
            userNotice.markClosingNotified();
        }

        return saveAndToPushes(notifications);
    }

    private Notification buildNotification(User user, Notice notice, NotificationType type, String title, String message) {
        return Notification.builder()
                .user(user)
                .notice(notice)
                .notificationType(type)
                .title(title)
                .message(message)
                .build();
    }

    private List<PendingPush> saveAndToPushes(List<Notification> notifications) {
        if (notifications.isEmpty()) {
            return List.of();
        }
        notificationRepository.saveAll(notifications);
        return notifications.stream()
                .map(n -> new PendingPush(n.getUser().getId(), n.getUser().getPushToken(),
                        n.getTitle(), n.getMessage(), n.getNotice().getId()))
                .toList();
    }

    // 트랜잭션 밖(FCM 발송 이후)에서 호출된다는 전제 — 실패한 유저 id만 별도 트랜잭션으로 정리한다
    @Transactional
    public void clearPushTokens(List<Long> userIds) {
        if (userIds.isEmpty()) {
            return;
        }
        userRepository.findAllById(userIds).forEach(user -> user.updatePushToken(null));
    }
}
