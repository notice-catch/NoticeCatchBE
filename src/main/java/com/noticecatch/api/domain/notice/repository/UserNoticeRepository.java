package com.noticecatch.api.domain.notice.repository;

import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserNoticeRepository extends JpaRepository<UserNotice, Long> {

    long countByUserAndIsScrapped(User user, boolean isScrapped);

    long countByUserAndIsRead(User user, boolean isRead);

    Optional<UserNotice> findByUserIdAndNoticeId(Long userId, Long noticeId);

    long countByUserIdAndIsScrappedTrue(Long userId);

    // 카테고리별 스크랩 개수를 한 번에 집계 (카테고리마다 개별 COUNT 쿼리 방지)
    @Query("SELECT n.category.name AS category, COUNT(un) AS count FROM UserNotice un " +
            "JOIN un.notice n WHERE un.user.id = :userId AND un.isScrapped = true GROUP BY n.category.name")
    List<CategoryCount> countGroupedByCategoryForUser(@Param("userId") Long userId);

    interface CategoryCount {
        String getCategory();

        long getCount();
    }

    @Query("SELECT n FROM UserNotice un JOIN un.notice n " +
            "JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE un.user.id = :userId AND un.isScrapped = true")
    Slice<Notice> findScrappedNoticesByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT n FROM UserNotice un JOIN un.notice n " +
            "JOIN FETCH n.category c JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE un.user.id = :userId AND un.isScrapped = true AND c.name = :categoryName")
    Slice<Notice> findScrappedNoticesByUserIdAndCategoryName(@Param("userId") Long userId, @Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT DISTINCT FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') " +
            "FROM UserNotice un JOIN un.notice n " +
            "WHERE un.user.id = :userId AND un.isScrapped = true " +
            "AND n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m') = :yearMonth " +
            "ORDER BY FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') ASC")
    List<String> findDistinctDeadlineDatesByUserIdAndYearMonth(@Param("userId") Long userId, @Param("yearMonth") String yearMonth);

    @Query("SELECT n FROM UserNotice un JOIN un.notice n " +
            "JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE un.user.id = :userId AND un.isScrapped = true " +
            "AND n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') = :date")
    Slice<Notice> findScrappedNoticesByUserIdAndDeadlineDate(@Param("userId") Long userId, @Param("date") String date, Pageable pageable);

    @Query("SELECT n FROM UserNotice un JOIN un.notice n " +
            "JOIN FETCH n.category JOIN FETCH n.university LEFT JOIN FETCH n.department " +
            "WHERE un.user.id = :userId AND un.isScrapped = true " +
            "AND n.deadlineAt IS NULL")
    Slice<Notice> findScrappedNoticesByUserIdAndDeadlineIsNull(@Param("userId") Long userId, Pageable pageable);

    // 마감임박 알림 배치 대상 — 스크랩했고, 아직 마감임박 알림 안 보냈고, 마감이 threshold 이내인 것
    @Query("SELECT un FROM UserNotice un " +
            "JOIN FETCH un.notice n JOIN FETCH un.user u " +
            "WHERE un.isScrapped = true AND un.closingNotified = false " +
            "AND n.deadlineAt IS NOT NULL AND n.deadlineAt BETWEEN :now AND :threshold " +
            "AND u.allNotification = true AND u.closingNotification = true")
    List<UserNotice> findClosingSoonCandidates(@Param("now") LocalDateTime now, @Param("threshold") LocalDateTime threshold);
}
