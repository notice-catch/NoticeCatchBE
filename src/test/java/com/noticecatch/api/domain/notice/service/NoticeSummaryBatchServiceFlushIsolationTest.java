package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.entity.Category;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.university.entity.University;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoticeSummaryBatchServiceFlushIsolationTest {

    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void 한_건의_저장_실패가_같은_배치의_다른_정상_건까지_롤백시키지_않는다() {
        University university = University.builder().name("테스트대학").build();
        entityManager.persist(university);
        Category category = Category.builder().name("장학").build();
        entityManager.persist(category);

        Notice failing = Notice.builder()
                .title("실패공지").content("본문").university(university).category(category).build();
        Notice succeeding = Notice.builder()
                .title("성공공지").content("본문").university(university).category(category).build();
        entityManager.persist(failing);
        entityManager.persist(succeeding);
        entityManager.flush();
        entityManager.clear();

        GeminiSummaryService geminiSummaryService = mock(GeminiSummaryService.class);
        String tooLong = "가".repeat(300); // eligibility 컬럼 길이(255)를 넘는 값
        given(geminiSummaryService.generateSummary("실패공지", "본문"))
                .willReturn(AiSummaryDto.builder().eligibility(tooLong).benefit("혜택").deadline("마감").build());
        given(geminiSummaryService.generateSummary("성공공지", "본문"))
                .willReturn(AiSummaryDto.builder().eligibility("자격").benefit("혜택").deadline("마감").build());

        NoticeSummaryBatchService batchService = new NoticeSummaryBatchService(noticeRepository, geminiSummaryService, entityManager);

        assertThatCode(batchService::summarizePendingNotices).doesNotThrowAnyException();

        entityManager.clear();
        Notice reloadedSucceeding = noticeRepository.findById(succeeding.getId()).orElseThrow();
        assertThat(reloadedSucceeding.getNoticeSummary()).isNotNull();
        assertThat(reloadedSucceeding.getNoticeSummary().getEligibility()).isEqualTo("자격");

        Notice reloadedFailing = noticeRepository.findById(failing.getId()).orElseThrow();
        assertThat(reloadedFailing.getNoticeSummary()).isNull();
    }
}
