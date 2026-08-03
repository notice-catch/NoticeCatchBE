package com.noticecatch.api.domain.notification.service;

import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int CLOSING_SOON_THRESHOLD_DAYS = 3;
    private static final DateTimeFormatter DEADLINE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NoticeRepository noticeRepository;
    private final UserKeywordRepository userKeywordRepository;
    private final UserNoticeRepository userNoticeRepository;
    private final FcmSender fcmSender;

    @Transactional
    public void registerDeviceToken(Long userId, DeviceTokenRequest request) {
        if (request == null || request.getPushToken() == null || request.getPushToken().isBlank()) {
            throw new ProjectException(NotificationErrorCode.PUSH_TOKEN_INVALID);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        user.updatePushToken(request.getPushToken());
    }

    public NotificationListResponse getNotifications(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        var notificationSlice = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        return NotificationListResponse.from(notificationSlice);
    }

    @Transactional
    public void readAllNotifications(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    // 크롤러가 DB에 직접 넣는 신규 공지를 5분마다 스캔해 키워드 알림을 발송한다
    @Transactional
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void notifyPendingNotices() {
        List<Notice> pendingNotices = noticeRepository.findByNotifiedFalse();
        for (Notice notice : pendingNotices) {
            notifyKeywordMatches(notice);
            notifyCategoryMatches(notice);
            notice.markNotified();
        }
    }

    private void notifyKeywordMatches(Notice notice) {
        Long universityId = notice.getUniversity().getId();
        List<User> candidates = userRepository
                .findByDepartment_University_IdAndAllNotificationTrueAndKeywordNotificationTrue(universityId);

        for (User user : candidates) {
            if (!matchesDepartment(user, notice)) {
                continue;
            }

            String matchedKeyword = findMatchedKeyword(user, notice);
            if (matchedKeyword == null) {
                continue;
            }

            // 알림함 예시(docs/API_SPEC.md)와 동일한 고정 문구
            Notification notification = Notification.builder()
                    .user(user)
                    .notice(notice)
                    .notificationType(NotificationType.KEYWORD)
                    .title("🏷️ 내 키워드와 관련된 공지가 등록되었어요")
                    .message(notice.getTitle())
                    .build();
            notificationRepository.save(notification);

            fcmSender.send(user, notification.getTitle(), notification.getMessage(), notice.getId());
        }
    }

    // 관심 카테고리(장학/학사/취업/비교과) 알림 — 유저가 켜둔 카테고리와 공지의 카테고리가 일치하면 발송
    private void notifyCategoryMatches(Notice notice) {
        Long universityId = notice.getUniversity().getId();
        String categoryName = notice.getCategory().getName();
        List<User> candidates = userRepository.findByDepartment_University_IdAndAllNotificationTrue(universityId);

        for (User user : candidates) {
            if (!matchesDepartment(user, notice) || !matchesCategoryPreference(user, categoryName)) {
                continue;
            }

            Notification notification = Notification.builder()
                    .user(user)
                    .notice(notice)
                    .notificationType(NotificationType.CATEGORY)
                    .title("📢 관심 카테고리에 새 공지가 등록되었어요")
                    .message(notice.getTitle())
                    .build();
            notificationRepository.save(notification);

            fcmSender.send(user, notification.getTitle(), notification.getMessage(), notice.getId());
        }
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

    private String findMatchedKeyword(User user, Notice notice) {
        List<UserKeyword> keywords = userKeywordRepository.findAllByUser(user);
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
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyClosingSoonNotices() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threshold = now.plusDays(CLOSING_SOON_THRESHOLD_DAYS);

        List<UserNotice> candidates = userNoticeRepository.findClosingSoonCandidates(now, threshold);
        for (UserNotice userNotice : candidates) {
            User user = userNotice.getUser();
            Notice notice = userNotice.getNotice();

            long daysLeft = ChronoUnit.DAYS.between(now.toLocalDate(), notice.getDeadlineAt().toLocalDate());
            String deadlineDate = notice.getDeadlineAt().format(DEADLINE_DATE_FORMAT);

            // 알림함 예시(docs/API_SPEC.md)와 동일한 고정 문구/포맷
            Notification notification = Notification.builder()
                    .user(user)
                    .notice(notice)
                    .notificationType(NotificationType.CLOSING)
                    .title("📌 곧 마감되는 공지가 있어요")
                    .message("[" + notice.getTitle() + "] D-" + daysLeft + " · " + deadlineDate + " 까지")
                    .build();
            notificationRepository.save(notification);

            fcmSender.send(user, notification.getTitle(), notification.getMessage(), notice.getId());
            userNotice.markClosingNotified();
        }
    }
}