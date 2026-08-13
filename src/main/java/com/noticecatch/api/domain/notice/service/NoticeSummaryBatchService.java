package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

// 크롤러가 DB에 직접 insert하는 신규 공지 중 AI 요약이 아직 없는 건을 30초마다 스캔해 Gemini 요약을 생성·저장한다.
// 주기(30초)당 처리 건수(BATCH_SIZE)를 곱한 값이 분당 15회를 넘지 않게 제한한다.
// 이 메서드 자체는 트랜잭션 없이 실행된다 — Gemini 호출(블로킹 네트워크)이 끝난 뒤
// 공지 1건의 저장만 NoticeSummaryPersistenceService가 REQUIRES_NEW로 커밋하므로,
// DB 커넥션이 네트워크 호출 시간만큼 잡혀있지 않고, 한 건의 저장 실패가 다른 건에 전파되지도 않는다.
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSummaryBatchService {

    private static final int BATCH_SIZE = 5;

    private final NoticeRepository noticeRepository;
    private final GeminiSummaryService geminiSummaryService;
    private final NoticeSummaryPersistenceService noticeSummaryPersistenceService;

    @Scheduled(fixedDelay = 30 * 1000)
    public void summarizePendingNotices() {
        List<Notice> pendingNotices = noticeRepository.findByNoticeSummaryIsNull(PageRequest.of(0, BATCH_SIZE));

        for (Notice notice : pendingNotices) {
            try {
                AiSummaryDto summary = geminiSummaryService.generateSummary(notice.getTitle(), notice.getContent());
                noticeSummaryPersistenceService.saveSummary(notice.getId(), summary);
                log.info("공지(id={}) AI 요약 생성 성공", notice.getId());
            } catch (Exception e) {
                // 실패한 공지는 noticeSummary가 여전히 null이므로 다음 배치 주기에 자동으로 재시도된다
                log.warn("공지(id={}) AI 요약 생성 실패, 다음 배치에서 재시도합니다: {}", notice.getId(), e.getMessage());
            }
        }
    }
}
