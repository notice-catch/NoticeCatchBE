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
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// saveSummary가 REQUIRES_NEW로 커밋되는지, 즉 같은 배치 안에서 한 건이 실패해도
// 이미 처리된 다른 건의 커밋 결과에 영향을 주지 않는지 실제 트랜잭션 경계로 검증한다.
// NoticeSummaryBatchService를 직접 new해서 부르던 이전 버전은 Spring 트랜잭션 프록시를 거치지 않아
// 이 격리가 실제로 동작하는지 검증하지 못했다 — 여기서는 @Import로 실제 빈을 등록해 프록시를 태운다.
// 클래스 레벨 @Transactional(NOT_SUPPORTED)로 @DataJpaTest의 기본 테스트 트랜잭션(테스트 종료 후 롤백)을
// 꺼둔다 — 그 트랜잭션이 살아있으면 fixture insert가 커밋되지 않아, REQUIRES_NEW가 별도 커넥션에서
// 시작하는 saveSummary 트랜잭션이 애초에 그 fixture를 찾지 못한다(가시성 문제로 오탐).
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(NoticeSummaryPersistenceService.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class NoticeSummaryPersistenceServiceIsolationTest {

    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private NoticeSummaryPersistenceService noticeSummaryPersistenceService;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @Test
    void 한_건의_저장_실패가_이미_커밋된_다른_건을_롤백시키지_않는다() {
        Long[] ids = new TransactionTemplate(transactionManager).execute(status -> {
            University university = University.builder().name("테스트대학").build();
            entityManager.persist(university);
            Category category = Category.builder().name("장학").build();
            entityManager.persist(category);

            Notice succeeding = Notice.builder()
                    .title("성공공지").content("본문").university(university).category(category).build();
            Notice failing = Notice.builder()
                    .title("실패공지").content("본문").university(university).category(category).build();
            entityManager.persist(succeeding);
            entityManager.persist(failing);
            return new Long[]{succeeding.getId(), failing.getId()};
        });
        Long succeedingId = ids[0];
        Long failingId = ids[1];

        AiSummaryDto okSummary = AiSummaryDto.builder().eligibility("자격").benefit("혜택").deadline("마감").build();
        String tooLong = "가".repeat(300); // eligibility 컬럼 길이(255)를 넘는 값
        AiSummaryDto badSummary = AiSummaryDto.builder().eligibility(tooLong).benefit("혜택").deadline("마감").build();

        // 1건은 성공 — 자체 트랜잭션(REQUIRES_NEW)이라 이 시점에 실제로 커밋된다
        noticeSummaryPersistenceService.saveSummary(succeedingId, okSummary);

        // 다른 1건은 실패 — 별도 트랜잭션이므로 위에서 이미 커밋된 succeeding에는 영향이 없어야 한다
        assertThatThrownBy(() -> noticeSummaryPersistenceService.saveSummary(failingId, badSummary))
                .isInstanceOf(RuntimeException.class);

        Notice reloadedSucceeding = noticeRepository.findById(succeedingId).orElseThrow();
        assertThat(reloadedSucceeding.getNoticeSummary()).isNotNull();
        assertThat(reloadedSucceeding.getNoticeSummary().getEligibility()).isEqualTo("자격");

        Notice reloadedFailing = noticeRepository.findById(failingId).orElseThrow();
        assertThat(reloadedFailing.getNoticeSummary()).isNull();
    }
}
