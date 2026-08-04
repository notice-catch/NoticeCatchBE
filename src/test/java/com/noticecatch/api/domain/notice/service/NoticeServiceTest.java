package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeSearchItemResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeSearchListResponse;
import com.noticecatch.api.domain.notice.entity.Category;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.exception.NoticeErrorCode;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.university.entity.University;
import com.noticecatch.api.domain.university.exception.UniversityErrorCode;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private UserNoticeRepository userNoticeRepository;
    @Mock
    private UserRepository userRepository;

    private NoticeService noticeService;

    @BeforeEach
    void setUp() {
        noticeService = new NoticeService(noticeRepository, userNoticeRepository, userRepository);
    }

    private University university(Long id) {
        return University.builder().id(id).name("가천대학교").build();
    }

    private Department department(Long id, University university) {
        return Department.builder().id(id).university(university).name("컴퓨터공학과").build();
    }

    private User userWithDepartment(Long id, Department department) {
        return User.builder().id(id).email("a@a.com").nickname("유저").department(department).build();
    }

    private Notice notice(Long id, University university, Category category) {
        return Notice.builder()
                .id(id)
                .university(university)
                .category(category)
                .title("공지 제목")
                .content("공지 내용")
                .postedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void 학과가_없는_유저가_공지_목록을_조회하면_UNIVERSITY_NOT_FOUND를_던진다() {
        User user = userWithDepartment(1L, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> noticeService.getNotices(1L, 0, 10, null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UniversityErrorCode.UNIVERSITY_NOT_FOUND);

        verify(noticeRepository, never()).findByUniversity_Id(any(), any());
    }

    @Test
    void 존재하지_않는_유저가_공지_목록을_조회하면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNotices(999L, 0, 10, null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 키워드가_없으면_대학_전체_공지목록을_조회한다() {
        University univ = university(1L);
        User user = userWithDepartment(1L, department(1L, univ));
        Category category = Category.builder().id(1L).name("장학").build();
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(10L, univ, category)));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.findByUniversity_Id(eq(1L), any(PageRequest.class))).willReturn(slice);

        SliceResponse<NoticeSearchItemResponse> response = noticeService.getNotices(1L, 0, 10, null);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getNoticeId()).isEqualTo(10L);
        verify(noticeRepository, never()).findByUniversity_IdAndCategory_Name(any(), any(), any());
    }

    @Test
    void 키워드가_있으면_카테고리로_필터링한_공지목록을_조회한다() {
        University univ = university(1L);
        User user = userWithDepartment(1L, department(1L, univ));
        Category category = Category.builder().id(1L).name("장학").build();
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(11L, univ, category)));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.findByUniversity_IdAndCategory_Name(eq(1L), eq("장학"), any(PageRequest.class)))
                .willReturn(slice);

        SliceResponse<NoticeSearchItemResponse> response = noticeService.getNotices(1L, 0, 10, " 장학 ");

        assertThat(response.getContent()).hasSize(1);
        verify(noticeRepository).findByUniversity_IdAndCategory_Name(eq(1L), eq("장학"), any(PageRequest.class));
    }

    @Test
    void 공지_상세조회시_로그인하지_않았으면_스크랩여부는_false다() {
        Notice notice = notice(1L, university(1L), Category.builder().id(1L).name("장학").build());
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));

        NoticeDetailResponse response = noticeService.getNoticeDetail(null, 1L);

        assertThat(response.getIsScrapped()).isFalse();
        verify(userNoticeRepository, never()).findByUserIdAndNoticeId(any(), any());
    }

    @Test
    void 공지_상세조회시_스크랩한_유저면_isScrapped_true다() {
        Notice notice = notice(1L, university(1L), Category.builder().id(1L).name("장학").build());
        UserNotice userNotice = UserNotice.builder().isScrapped(true).build();
        given(noticeRepository.findById(1L)).willReturn(Optional.of(notice));
        given(userNoticeRepository.findByUserIdAndNoticeId(1L, 1L)).willReturn(Optional.of(userNotice));

        NoticeDetailResponse response = noticeService.getNoticeDetail(1L, 1L);

        assertThat(response.getIsScrapped()).isTrue();
    }

    @Test
    void 존재하지_않는_공지_상세조회시_NOTICE_NOT_FOUND를_던진다() {
        given(noticeRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> noticeService.getNoticeDetail(1L, 999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(NoticeErrorCode.NOTICE_NOT_FOUND);
    }

    @Test
    void 검색어가_공백_제외_2글자_미만이면_SEARCH_KEYWORD_TOO_SHORT를_던진다() {
        University univ = university(1L);
        User user = userWithDepartment(1L, department(1L, univ));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> noticeService.searchNotices(1L, " a ", "latest", 0, 10))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(NoticeErrorCode.SEARCH_KEYWORD_TOO_SHORT);

        verify(noticeRepository, never()).searchByKeywordAndUniversityId(any(), any(), any());
    }

    @Test
    void 검색어가_null이면_SEARCH_KEYWORD_TOO_SHORT를_던진다() {
        University univ = university(1L);
        User user = userWithDepartment(1L, department(1L, univ));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> noticeService.searchNotices(1L, null, "latest", 0, 10))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(NoticeErrorCode.SEARCH_KEYWORD_TOO_SHORT);
    }

    @Test
    void 정상_검색어면_공지를_검색해서_반환한다() {
        University univ = university(1L);
        User user = userWithDepartment(1L, department(1L, univ));
        Category category = Category.builder().id(1L).name("장학").build();
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(20L, univ, category)));

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(noticeRepository.searchByKeywordAndUniversityId(eq(1L), eq("장학금"), any(PageRequest.class)))
                .willReturn(slice);

        NoticeSearchListResponse response = noticeService.searchNotices(1L, " 장학금 ", "deadline", 0, 10);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getNoticeId()).isEqualTo(20L);
    }

    @Test
    void 월이_한자리면_0을_채워서_yearMonth를_조회한다() {
        given(noticeRepository.findDistinctDeadlineDatesByYearMonth("2026-08")).willReturn(List.of("2026-08-15"));

        List<String> dates = noticeService.getCalendarDates("2026", "8");

        assertThat(dates).containsExactly("2026-08-15");
        verify(noticeRepository).findDistinctDeadlineDatesByYearMonth("2026-08");
    }

    @Test
    void 월이_두자리면_그대로_yearMonth를_조회한다() {
        given(noticeRepository.findDistinctDeadlineDatesByYearMonth("2026-12")).willReturn(List.of());

        noticeService.getCalendarDates("2026", "12");

        verify(noticeRepository).findDistinctDeadlineDatesByYearMonth("2026-12");
    }

    @Test
    void 특정_날짜의_캘린더_공지목록을_조회한다() {
        University univ = university(1L);
        Category category = Category.builder().id(1L).name("장학").build();
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(30L, univ, category)));
        given(noticeRepository.findByDeadlineAtDate(eq("2026-08-15"), any(PageRequest.class))).willReturn(slice);

        Slice<NoticeSearchItemResponse> response = noticeService.getCalendarNotices("2026-08-15", 0, 10);

        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void 마감일이_없는_공지목록을_조회한다() {
        University univ = university(1L);
        Category category = Category.builder().id(1L).name("장학").build();
        Slice<Notice> slice = new SliceImpl<>(List.of(notice(40L, univ, category)));
        given(noticeRepository.findByDeadlineAtIsNull(any(PageRequest.class))).willReturn(slice);

        Slice<NoticeSearchItemResponse> response = noticeService.getNoDeadlineNotices(0, 10);

        assertThat(response.getContent()).hasSize(1);
    }
}
