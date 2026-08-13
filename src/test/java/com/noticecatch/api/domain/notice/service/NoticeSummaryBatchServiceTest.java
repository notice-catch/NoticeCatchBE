package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NoticeSummaryBatchServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private GeminiSummaryService geminiSummaryService;
    @Mock
    private NoticeSummaryPersistenceService noticeSummaryPersistenceService;

    private NoticeSummaryBatchService noticeSummaryBatchService;

    @BeforeEach
    void setUp() {
        noticeSummaryBatchService = new NoticeSummaryBatchService(
                noticeRepository, geminiSummaryService, noticeSummaryPersistenceService);
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

        verify(geminiSummaryService, never()).generateSummary(any(), any());
        verify(noticeSummaryPersistenceService, never()).saveSummary(any(), any());
    }

    @Test
    void 요약_생성에_성공하면_건별_저장을_별도_트랜잭션에_위임한다() {
        Notice notice = notice(1L, "장학금 안내", "본문 내용");
        AiSummaryDto summary = summary();
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class))).willReturn(List.of(notice));
        given(geminiSummaryService.generateSummary("장학금 안내", "본문 내용")).willReturn(summary);

        noticeSummaryBatchService.summarizePendingNotices();

        verify(noticeSummaryPersistenceService).saveSummary(1L, summary);
    }

    @Test
    void Gemini_호출이_실패한_공지는_저장을_시도하지_않고_나머지는_정상_처리된다() {
        Notice failing = notice(1L, "실패 공지", "본문1");
        Notice succeeding = notice(2L, "성공 공지", "본문2");
        AiSummaryDto succeedingSummary = summary();
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class)))
                .willReturn(List.of(failing, succeeding));
        given(geminiSummaryService.generateSummary("실패 공지", "본문1"))
                .willThrow(new RuntimeException("Gemini 호출 실패", new RuntimeException("timeout")));
        given(geminiSummaryService.generateSummary("성공 공지", "본문2")).willReturn(succeedingSummary);

        assertThatCode(() -> noticeSummaryBatchService.summarizePendingNotices()).doesNotThrowAnyException();

        verify(noticeSummaryPersistenceService, never()).saveSummary(eq(1L), any());
        verify(noticeSummaryPersistenceService).saveSummary(2L, succeedingSummary);
    }

    @Test
    void 한_건의_저장_실패가_같은_배치의_다른_건_처리를_막지_않는다() {
        Notice failing = notice(1L, "저장 실패 공지", "본문1");
        Notice succeeding = notice(2L, "저장 성공 공지", "본문2");
        AiSummaryDto failingSummary = summary();
        AiSummaryDto succeedingSummary = summary();
        given(noticeRepository.findByNoticeSummaryIsNull(any(PageRequest.class)))
                .willReturn(List.of(failing, succeeding));
        given(geminiSummaryService.generateSummary("저장 실패 공지", "본문1")).willReturn(failingSummary);
        given(geminiSummaryService.generateSummary("저장 성공 공지", "본문2")).willReturn(succeedingSummary);
        doThrow(new RuntimeException("DB 저장 실패")).when(noticeSummaryPersistenceService).saveSummary(1L, failingSummary);

        assertThatCode(() -> noticeSummaryBatchService.summarizePendingNotices()).doesNotThrowAnyException();

        verify(noticeSummaryPersistenceService).saveSummary(2L, succeedingSummary);
    }
}
