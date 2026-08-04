package com.noticecatch.api.domain.notification.service;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.notice.entity.Category;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.notification.entity.Notification;
import com.noticecatch.api.domain.notification.entity.NotificationType;
import com.noticecatch.api.domain.notification.repository.NotificationRepository;
import com.noticecatch.api.domain.university.entity.University;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationBatchServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserKeywordRepository userKeywordRepository;
    @Mock
    private UserNoticeRepository userNoticeRepository;

    private NotificationBatchService notificationBatchService;

    private University university;
    private Department department;
    private Category scholarshipCategory;

    @BeforeEach
    void setUp() {
        notificationBatchService = new NotificationBatchService(
                notificationRepository, userRepository, noticeRepository,
                userKeywordRepository, userNoticeRepository);

        university = University.builder().id(1L).name("테스트대학교").build();
        department = Department.builder().id(1L).university(university).name("컴퓨터공학과").build();
        scholarshipCategory = Category.builder().id(1L).name("장학").build();
    }

    private User user(Long id, Department dept, boolean keywordNotification, boolean scholarship) {
        return User.builder()
                .id(id)
                .department(dept)
                .email("user" + id + "@example.com")
                .nickname("유저" + id)
                .allNotification(true)
                .keywordNotification(keywordNotification)
                .scholarship(scholarship)
                .extracurricular(false)
                .academic(false)
                .employment(false)
                .pushToken("token-" + id)
                .build();
    }

    private Notice notice(Long id, Department dept, Category category, String title, String content) {
        return Notice.builder()
                .id(id)
                .university(university)
                .category(category)
                .department(dept)
                .title(title)
                .content(content)
                .notified(false)
                .build();
    }

    @Test
    void 미처리_공지가_없으면_아무일도_하지_않는다() {
        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of());

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        assertThat(pushes).isEmpty();
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void 매칭되는_유저가_없으면_알림없이_notified만_true로_바뀐다() {
        Notice notice = notice(1L, department, scholarshipCategory, "장학금 안내", "내용");
        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of());

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        assertThat(pushes).isEmpty();
        verify(notificationRepository, never()).saveAll(any());
        assertThat(notice.getNotified()).isTrue();
    }

    @Test
    void 키워드가_제목에_포함되면_KEYWORD_알림을_생성하고_발송대상에_포함한다() {
        Notice notice = notice(1L, department, scholarshipCategory, "2026 장학금 신청 안내", "내용");
        User user = user(1L, department, true, false); // 카테고리는 안 맞고 키워드만
        UserKeyword keyword = UserKeyword.builder().id(1L).user(user).keyword("장학금").keywordType("CUSTOM").build();

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(user));
        given(userKeywordRepository.findAllByUserIn(List.of(user))).willReturn(List.of(keyword));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        List<Notification> saved = captor.getValue();

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getNotificationType()).isEqualTo(NotificationType.KEYWORD);
        assertThat(saved.get(0).getMessage()).isEqualTo(notice.getTitle());

        assertThat(pushes).hasSize(1);
        PendingPush push = pushes.get(0);
        assertThat(push.userId()).isEqualTo(1L);
        assertThat(push.pushToken()).isEqualTo("token-1");
        assertThat(push.noticeId()).isEqualTo(1L);
    }

    @Test
    void 키워드알림이_꺼진_유저는_제목이_일치해도_KEYWORD_알림을_안_받는다() {
        Notice notice = notice(1L, department, scholarshipCategory, "장학금 안내", "내용");
        User user = user(1L, department, false, false); // keywordNotification = false

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(user));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        // 후보가 있으면 키워드는 일괄 조회되지만(배치 효율화), keywordNotification=false라 매칭엔 안 쓰인다.
        // 매칭된 알림이 없으므로 saveAll 자체도 호출되지 않는다.
        assertThat(pushes).isEmpty();
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void 카테고리_설정이_켜진_유저는_키워드_없이도_CATEGORY_알림을_받는다() {
        Notice notice = notice(1L, department, scholarshipCategory, "장학금 안내", "내용");
        User user = user(1L, department, false, true); // scholarship = true, keywordNotification 무관

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(user));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getNotificationType()).isEqualTo(NotificationType.CATEGORY);
        assertThat(pushes).hasSize(1);
    }

    @Test
    void 키워드와_카테고리가_동시에_맞으면_알림_두개를_받는다() {
        Notice notice = notice(1L, department, scholarshipCategory, "장학금 신청 안내", "내용");
        User user = user(1L, department, true, true);
        UserKeyword keyword = UserKeyword.builder().id(1L).user(user).keyword("장학금").keywordType("CUSTOM").build();

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(user));
        given(userKeywordRepository.findAllByUserIn(List.of(user))).willReturn(List.of(keyword));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).extracting(Notification::getNotificationType)
                .containsExactlyInAnyOrder(NotificationType.KEYWORD, NotificationType.CATEGORY);
        assertThat(pushes).hasSize(2);
    }

    @Test
    void 학과공지는_다른학과_유저에게_안간다() {
        Department otherDept = Department.builder().id(2L).university(university).name("전자공학과").build();
        Notice notice = notice(1L, department, scholarshipCategory, "컴공 장학금 안내", "내용");
        User otherDeptUser = user(1L, otherDept, false, true);

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(notice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(otherDeptUser));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        // 학과 필터링 후 후보가 아예 없으면 saveAll 자체를 호출하지 않고 조기 종료한다
        assertThat(pushes).isEmpty();
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void 전체공지는_학과_상관없이_대학_소속이면_다_받는다() {
        Notice universityWideNotice = notice(1L, null, scholarshipCategory, "전체 장학금 안내", "내용");
        User anyDeptUser = user(1L, department, false, true);

        given(noticeRepository.findByNotifiedFalse()).willReturn(List.of(universityWideNotice));
        given(userRepository.findByDepartment_University_IdAndAllNotificationTrue(1L)).willReturn(List.of(anyDeptUser));

        List<PendingPush> pushes = notificationBatchService.persistPendingNoticeNotifications();

        assertThat(pushes).hasSize(1);
    }

    @Test
    void 마감임박_알림은_Dday와_날짜를_스펙_포맷대로_만든다() {
        LocalDateTime deadline = LocalDateTime.now().plusDays(3).withHour(12).withMinute(0).withSecond(0).withNano(0);
        Notice notice = Notice.builder()
                .id(1L).university(university).category(scholarshipCategory).department(department)
                .title("수강신청 안내").content("내용").deadlineAt(deadline).notified(true)
                .build();
        User user = user(1L, department, false, false);
        UserNotice userNotice = UserNotice.builder().id(1L).user(user).notice(notice).isScrapped(true).isRead(false).closingNotified(false).build();

        given(userNoticeRepository.findClosingSoonCandidates(any(), any())).willReturn(List.of(userNotice));

        List<PendingPush> pushes = notificationBatchService.persistClosingSoonNotifications();

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepository).saveAll(captor.capture());
        Notification saved = captor.getValue().get(0);

        assertThat(saved.getNotificationType()).isEqualTo(NotificationType.CLOSING);
        assertThat(saved.getTitle()).isEqualTo("📌 곧 마감되는 공지가 있어요");
        String expectedDate = deadline.toLocalDate().toString();
        assertThat(saved.getMessage()).isEqualTo("[수강신청 안내] D-3 · " + expectedDate + " 까지");
        assertThat(userNotice.getClosingNotified()).isTrue();

        assertThat(pushes).hasSize(1);
        assertThat(pushes.get(0).userId()).isEqualTo(1L);
        assertThat(pushes.get(0).body()).isEqualTo("[수강신청 안내] D-3 · " + expectedDate + " 까지");
    }

    @Test
    void 마감임박_후보가_없으면_아무일도_하지_않는다() {
        given(userNoticeRepository.findClosingSoonCandidates(any(), any())).willReturn(List.of());

        List<PendingPush> pushes = notificationBatchService.persistClosingSoonNotifications();

        assertThat(pushes).isEmpty();
        verify(notificationRepository, never()).saveAll(any());
    }

    @Test
    void 유저_id_목록으로_pushToken을_초기화한다() {
        User user1 = user(1L, department, true, true);
        User user2 = user(2L, department, true, true);
        given(userRepository.findAllById(List.of(1L, 2L))).willReturn(List.of(user1, user2));

        notificationBatchService.clearPushTokens(List.of(1L, 2L));

        assertThat(user1.getPushToken()).isNull();
        assertThat(user2.getPushToken()).isNull();
    }

    @Test
    void 빈_유저_id_목록이면_조회조차_하지_않는다() {
        notificationBatchService.clearPushTokens(List.of());

        verify(userRepository, never()).findAllById(any());
    }
}
