package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.NoticeSummary;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// 크롤러가 DB에 직접 insert하는 신규 공지 중 AI 요약이 아직 없는 건을 30초마다 스캔해 Gemini 요약을 생성·저장한다.
// 주기(30초)당 처리 건수(BATCH_SIZE)를 곱한 값이 분당 15회를 넘지 않게 제한한다.
@Slf4j
@Service
@RequiredArgsConstructor
public class NoticeSummaryBatchService {

    private static final int BATCH_SIZE = 5;

    private final NoticeRepository noticeRepository;
    private final GeminiSummaryService geminiSummaryService;
    private final EntityManager entityManager;

    @Transactional
    @Scheduled(fixedDelay = 30 * 1000)
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
                // 건별로 즉시 flush해서, 이 공지의 저장 실패가 같은 배치의 다른 건까지 롤백시키지 않게 격리한다
                noticeRepository.saveAndFlush(notice);
                log.info("공지(id={}) AI 요약 생성 성공", notice.getId());
            } catch (Exception e) {
                // 실패한 공지는 noticeSummary가 여전히 null이므로 다음 배치 주기에 자동으로 재시도된다
                log.warn("공지(id={}) AI 요약 생성 실패, 다음 배치에서 재시도합니다: {}", notice.getId(), e.getMessage());
                // flush 실패 시 이 공지의 NoticeSummary가 영속성 컨텍스트에 dirty 상태로 남아,
                // 다음 건의 flush에 계속 같이 딸려가 재시도되며 엉뚱한 공지가 실패로 오인되는 것을 방지한다
                entityManager.clear();
            }
        }
    }
}
