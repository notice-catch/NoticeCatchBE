package com.noticecatch.api.domain.notice.repository;

import com.noticecatch.api.domain.notice.entity.NoticeSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeSummaryRepository extends JpaRepository<NoticeSummary, Long> {
}
