package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.CalendarDatesResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeScrapListResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeScrapResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeSearchItemResponse;
import com.noticecatch.api.domain.notice.entity.Category;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.exception.NoticeErrorCode;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.university.entity.University;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserNoticeServiceTest {

    @Mock
    private UserNoticeRepository userNoticeRepository;
    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserRepository userRepository;

    private UserNoticeService userNoticeService;

    @BeforeEach
    void setUp() {
        userNoticeService = new UserNoticeService(userNoticeRepository, noticeRepository, userRepository);
    }

    private User user(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").build();
    }

    private User withdrawnUser(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").status("WITHDRAWN").build();
    }

    private Notice notice(Long id) {
        University university = University.builder().id(1L).name("가천대학교").build();
        Category category = Category.builder().id(1L).name("장학").build();
        return Notice.builder().id(id).university(university).category(category).title("제목").build();
    }

    private UserNoticeRepository.CategoryCount categoryCount(String category, long count) {
        return new UserNoticeRepository.CategoryCount() {
            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    @Test
    void 스크랩_이력이_없으면_새로_생성하고_스크랩_처리한다() {
        User user = user(1L);
        Notice notice = notice(10L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.findById(10L)).willReturn(Optional.of(notice));
        given(userNoticeRepository.findByUserIdAndNoticeId(1L, 10L)).willReturn(Optional.empty());
        given(userNoticeRepository.saveAndFlush(any(UserNotice.class))).willAnswer(invocation -> invocation.getArgument(0));

        NoticeScrapResponse response = userNoticeService.scrapNotice(1L, 10L);

        assertThat(response.getNoticeId()).isEqualTo(10L);
        assertThat(response.getIsScraped()).isTrue();
        verify(userNoticeRepository).saveAndFlush(any(UserNotice.class));
    }

    @Test
    void 동시_스크랩_요청으로_유니크_제약_위반시_먼저_저장된_행을_재조회해_이어서_처리한다() {
        User user = user(1L);
        Notice notice = notice(10L);
        UserNotice savedByOtherRequest = UserNotice.builder().user(user).notice(notice).isScrapped(false).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.findById(10L)).willReturn(Optional.of(notice));
        given(userNoticeRepository.findByUserIdAndNoticeId(1L, 10L))
                .willReturn(Optional.empty(), Optional.of(savedByOtherRequest));
        given(userNoticeRepository.saveAndFlush(any(UserNotice.class)))
                .willThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate"));

        NoticeScrapResponse response = userNoticeService.scrapNotice(1L, 10L);

        assertThat(response.getIsScraped()).isTrue();
    }

    @Test
    void 이미_스크랩된_공지면_스크랩을_취소한다() {
        User user = user(1L);
        Notice notice = notice(10L);
        UserNotice existing = UserNotice.builder().user(user).notice(notice).isScrapped(true).build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.findById(10L)).willReturn(Optional.of(notice));
        given(userNoticeRepository.findByUserIdAndNoticeId(1L, 10L)).willReturn(Optional.of(existing));

        NoticeScrapResponse response = userNoticeService.scrapNotice(1L, 10L);

        assertThat(response.getIsScraped()).isFalse();
        verify(userNoticeRepository, never()).saveAndFlush(any());
    }

    @Test
    void 존재하지_않는_유저가_스크랩하면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userNoticeService.scrapNotice(999L, 10L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 탈퇴한_유저가_스크랩하면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(withdrawnUser(1L)));

        assertThatThrownBy(() -> userNoticeService.scrapNotice(1L, 10L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(noticeRepository, never()).findById(any());
    }

    @Test
    void 존재하지_않는_공지를_스크랩하면_NOTICE_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user(1L)));
        given(noticeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userNoticeService.scrapNotice(1L, 999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(NoticeErrorCode.NOTICE_NOT_FOUND);
    }

    @Test
    void 카테고리_지정없이_스크랩목록을_조회하면_전체_스크랩목록과_카테고리별_개수를_반환한다() {
        given(userNoticeRepository.countGroupedByCategoryForUser(1L))
                .willReturn(List.of(categoryCount("장학", 2L), categoryCount("학사", 1L)));
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(10L)));
        given(userNoticeRepository.findScrappedNoticesByUserId(eq(1L), any(PageRequest.class))).willReturn(slice);

        NoticeScrapListResponse response = userNoticeService.getScrapNotices(1L, null, "latest", 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getCategoryCounts()).containsEntry("SCHOLARSHIP", 2L);
        assertThat(response.getCategoryCounts()).containsEntry("ACADEMIC", 1L);
        assertThat(response.getCategoryCounts()).containsEntry("ALL", 3L);
        verify(userNoticeRepository, never()).findScrappedNoticesByUserIdAndCategoryName(any(), any(), any());
    }

    @Test
    void 스크랩한_공지가_하나도_없어도_카테고리별_개수는_0으로_모두_채워진다() {
        given(userNoticeRepository.countGroupedByCategoryForUser(1L)).willReturn(List.of());
        given(userNoticeRepository.findScrappedNoticesByUserId(eq(1L), any(PageRequest.class)))
                .willReturn(new SliceImpl<>(List.of()));

        NoticeScrapListResponse response = userNoticeService.getScrapNotices(1L, null, "latest", 0, 10);

        assertThat(response.getCategoryCounts())
                .containsEntry("ALL", 0L)
                .containsEntry("SCHOLARSHIP", 0L)
                .containsEntry("ACADEMIC", 0L)
                .containsEntry("EMPLOYMENT", 0L);
    }

    @Test
    void 카테고리를_지정하면_해당_카테고리_스크랩목록만_조회한다() {
        given(userNoticeRepository.countGroupedByCategoryForUser(1L)).willReturn(List.of());
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(11L)));
        given(userNoticeRepository.findScrappedNoticesByUserIdAndCategoryName(eq(1L), eq("장학"), any(PageRequest.class)))
                .willReturn(slice);

        NoticeScrapListResponse response = userNoticeService.getScrapNotices(1L, "장학", "deadline", 0, 10);

        assertThat(response.getContent()).hasSize(1);
        verify(userNoticeRepository).findScrappedNoticesByUserIdAndCategoryName(eq(1L), eq("장학"), any(PageRequest.class));
    }

    @Test
    void 월이_한자리면_0을_채워서_스크랩_캘린더_날짜를_조회한다() {
        given(userNoticeRepository.findDistinctDeadlineDatesByUserIdAndYearMonth(1L, "2026-08"))
                .willReturn(List.of("2026-08-20"));

        CalendarDatesResponse response = userNoticeService.getCalendarDates(1L, "2026", "8");

        assertThat(response.getContent()).containsExactly("2026-08-20");
        assertThat(response.getTotalCount()).isEqualTo(1);
    }

    @Test
    void 특정_날짜의_스크랩_캘린더_공지목록을_조회한다() {
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(12L)));
        given(userNoticeRepository.findScrappedNoticesByUserIdAndDeadlineDate(eq(1L), eq("2026-08-20"), any(PageRequest.class)))
                .willReturn(slice);

        Slice<NoticeSearchItemResponse> response = userNoticeService.getCalendarNotices(1L, "2026-08-20", 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void 마감일_없는_스크랩_공지목록을_조회한다() {
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(13L)));
        given(userNoticeRepository.findScrappedNoticesByUserIdAndDeadlineIsNull(eq(1L), any(PageRequest.class)))
                .willReturn(slice);

        Slice<NoticeSearchItemResponse> response = userNoticeService.getNoDeadlineNotices(1L, 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }
}
