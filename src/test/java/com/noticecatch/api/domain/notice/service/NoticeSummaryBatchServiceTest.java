package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeSummaryBatchServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private GeminiSummaryService geminiSummaryService;
    @Mock
    private EntityManager entityManager;

    private NoticeSummaryBatchService noticeSummaryBatchService;

    @BeforeEach
    void setUp() {
        noticeSummaryBatchService = new NoticeSummaryBatchService(noticeRepository, geminiSummaryService, entityManager);
    }

    private Notice notice(Long id, String title, String content) {
        return Notice.builder().id(id).title(title).content(content).build();
    }

    private AiSummaryDto summary() {
        return AiSummaryDto.builder()
                .eligibility("직전 학기 12학점 이상 이수자")
                .benefit("등록금 전액 면제")
                .deadline("2026년 7월 9일까지")
                .build();
    }

    @Test
    void 요약이_없는_공지가_없으면_Gemini를_호출하지_않는다() {
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class))).willReturn(List.of());

        noticeSummaryBatchService.summarizePendingNotices();

        verify(geminiSummaryService, org.mockito.Mockito.never()).generateSummary(any(), any());
    }

    @Test
    void 요약_생성에_성공하면_공지에_요약이_저장된다() {
        Notice notice = notice(1L, "장학금 안내", "본문 내용");
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class))).willReturn(List.of(notice));
        given(geminiSummaryService.generateSummary("장학금 안내", "본문 내용")).willReturn(summary());

        noticeSummaryBatchService.summarizePendingNotices();

        assertThat(notice.getNoticeSummary()).isNotNull();
        assertThat(notice.getNoticeSummary().getEligibility()).isEqualTo("직전 학기 12학점 이상 이수자");
        assertThat(notice.getNoticeSummary().getBenefit()).isEqualTo("등록금 전액 면제");
        assertThat(notice.getNoticeSummary().getDeadlineSummary()).isEqualTo("2026년 7월 9일까지");
        assertThat(notice.getNoticeSummary().getNotice()).isSameAs(notice);
    }

    @Test
    void 일부_공지의_요약_생성이_실패해도_나머지는_정상_처리되고_예외가_전파되지_않는다() {
        Notice failing = notice(1L, "실패 공지", "본문1");
        Notice succeeding = notice(2L, "성공 공지", "본문2");
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class)))
                .willReturn(List.of(failing, succeeding));
        given(geminiSummaryService.generateSummary("실패 공지", "본문1"))
                .willThrow(new RuntimeException("Gemini 호출 실패", new RuntimeException("timeout")));
        given(geminiSummaryService.generateSummary("성공 공지", "본문2")).willReturn(summary());

        assertThatCode(() -> noticeSummaryBatchService.summarizePendingNotices()).doesNotThrowAnyException();

        assertThat(failing.getNoticeSummary()).isNull();
        assertThat(succeeding.getNoticeSummary()).isNotNull();
    }
}
