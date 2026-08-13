package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.NoticeSummary;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// 공지 1건의 AI 요약 저장을 자체 트랜잭션(REQUIRES_NEW)으로 커밋한다.
// NoticeSummaryBatchService가 배치 전체를 하나의 트랜잭션으로 묶으면, 한 건의 flush 실패가
// 트랜잭션을 rollback-only로 만들어 이미 성공한 다른 건까지 커밋 시점에 함께 롤백되는 문제가 있었다.
// 건별로 독립된 트랜잭션에서 커밋하면 실패가 다른 건에 전파되지 않는다.
@Service
@RequiredArgsConstructor
public class NoticeSummaryPersistenceService {

    private final NoticeRepository noticeRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSummary(Long noticeId, AiSummaryDto summary) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new IllegalStateException("공지(id=" + noticeId + ")를 찾을 수 없습니다"));
        notice.setNoticeSummary(NoticeSummary.builder()
                .eligibility(summary.getEligibility())
                .benefit(summary.getBenefit())
                .deadlineSummary(summary.getDeadline())
                .build());
    }
}
