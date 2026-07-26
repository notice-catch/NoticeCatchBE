package com.noticecatch.api.domain.notice.repository;

import com.noticecatch.api.domain.notice.entity.Notice;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    Slice<Notice> findByCategory_Name(String categoryName, Pageable pageable);

    @Query("SELECT n FROM Notice n " +
            "WHERE n.title LIKE %:keyword% OR n.content LIKE %:keyword%")
    Slice<Notice> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT DISTINCT FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') " +
            "FROM Notice n " +
            "WHERE n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m') = :yearMonth " +
            "ORDER BY FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') ASC")
    List<String> findDistinctDeadlineDatesByYearMonth(@Param("yearMonth") String yearMonth);

    @Query("SELECT n FROM Notice n " +
            "WHERE n.deadlineAt IS NOT NULL " +
            "AND FUNCTION('DATE_FORMAT', n.deadlineAt, '%Y-%m-%d') = :date")
    Slice<Notice> findByDeadlineAtDate(@Param("date") String date, Pageable pageable);

    Slice<Notice> findByDeadlineAtIsNull(Pageable pageable);
}