package com.noticecatch.api.domain.notice.repository;

import com.noticecatch.api.domain.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    // 상세 응답도 category/university/department를 읽으므로(NoticeDetailResponse.of) fetch join.
    // 전부 LEFT JOIN인 이유: findById()의 LAZY 프록시는 FK가 가리키는 row가 없으면(예: 크롤러가 잘못 채운 department_id)
    // 초기화 시점에 EntityNotFoundException으로 500을 유발하는데, LEFT JOIN FETCH는 대신 해당 연관관계를 null로 채운다.
    @Query("SELECT n FROM Notice n " +
            "LEFT JOIN FETCH n.category " +
            "LEFT JOIN FETCH n.university " +
            "LEFT JOIN FETCH n.department " +
            "WHERE n.id = :noticeId")
    Optional<Notice> findDetailById(@Param("noticeId") Long noticeId);

    // 목록 응답이 category/university/department를 매번 읽으므로(NoticeSearchItemResponse.from) 한 번에 fetch join
    @Query("SELECT n FROM Notice n JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE n.university.id = :universityId AND n.category.name = :categoryName")
    Slice<Notice> findByUniversity_IdAndCategory_Name(@Param("universityId") Long universityId,
                                                        @Param("categoryName") String categoryName,
                                                        Pageable pageable);

    @Query("SELECT n FROM Notice n JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE n.university.id = :universityId")
    Slice<Notice> findByUniversity_Id(@Param("universityId") Long universityId, Pageable pageable);

    @Query("SELECT n FROM Notice n JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE n.university.id = :universityId " +
            "AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%)")
    Slice<Notice> searchByKeywordAndUniversityId(@Param("universityId") Long universityId,
                                                 @Param("keyword") String keyword,
                                                 Pageable pageable);

    @Query("SELECT DISTINCT FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') " +
            "FROM Notice n " +
            "WHERE n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m') = :yearMonth " +
            "ORDER BY FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') ASC")
    List<String> findDistinctDeadlineDatesByYearMonth(@Param("yearMonth") String yearMonth);

    @Query("SELECT n FROM Notice n JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') = :date")
    Slice<Notice> findByDeadlineAtDate(@Param("date") String date, Pageable pageable);

    @Query("SELECT n FROM Notice n JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE n.deadlineAt IS NULL")
    Slice<Notice> findByDeadlineAtIsNull(Pageable pageable);

    // 알림 배치 대상 — 아직 알림 처리 안 한 공지. 매칭 중 university/category/department를 반드시 읽으므로 한 번에 fetch join
    @Query("SELECT n FROM Notice n " +
            "JOIN FETCH n.university " +
            "JOIN FETCH n.category " +
            "LEFT JOIN FETCH n.department " +
            "WHERE n.notified = false")
    List<Notice> findByNotifiedFalse();

    // AI 요약 배치 대상 — 아직 요약이 생성되지 않은 공지를 오래된 순으로 한 번에 BATCH_SIZE만큼만 가져온다
    @Query("SELECT n FROM Notice n WHERE n.noticeSummary IS NULL ORDER BY n.id ASC")
    List<Notice> findByNoticeSummaryIsNull(Pageable pageable);
}
