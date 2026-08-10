package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.NoticeSummary;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 크롤러가 DB에 직접 insert하는 신규 공지 중 AI 요약이 아직 없는 건을 5분마다 스캔해 Gemini 요약을 생성·저장한다.
// 한 번에 너무 많은 외부 API 호출로 배치가 오래 붙잡히지 않도록 주기당 처리 건수를 제한한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSummaryBatchService {

    private static final int BATCH_SIZE = 5;

    private final NoticeRepository noticeRepository;
    private final GeminiSummaryService geminiSummaryService;

    @Transactional
    @Scheduled(fixedDelay = 5 * 60 * 1000)
    public void summarizePendingNotices() {
        List<Notice> pendingNotices = noticeRepository.findByNoticeSummaryIsNull(PageRequest.of(0, BATCH_SIZE));

        for (Notice notice : pendingNotices) {
            try {
                AiSummaryDto summary = geminiSummaryService.generateSummary(notice.getTitle(), notice.getContent());
                notice.setNoticeSummary(NoticeSummary.builder()
                        .eligibility(summary.getEligibility())
                        .benefit(summary.getBenefit())
                        .deadlineSummary(summary.getDeadline())
                        .build());
            } catch (Exception e) {
                // 실패한 공지는 noticeSummary가 여전히 null이므로 다음 배치 주기에 자동으로 재시도된다
                log.warn("공지(id={}) AI 요약 생성 실패, 다음 배치에서 재시도합니다: {}", notice.getId(), e.getMessage());
            }
        }
    }
}
