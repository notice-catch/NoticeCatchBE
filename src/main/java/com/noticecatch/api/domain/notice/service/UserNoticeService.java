package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.*;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.exception.NoticeErrorCode;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserNoticeService {

    private static final Map<String, String> CATEGORY_NAME_TO_COUNT_KEY = Map.of(
            "장학", "SCHOLARSHIP",
            "학사", "ACADEMIC",
            "취업", "EMPLOYMENT"
    );

    private final UserNoticeRepository userNoticeRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoticeScrapResponse scrapNotice(Long userId, Long noticeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));

        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ProjectException(NoticeErrorCode.NOTICE_NOT_FOUND));

        UserNotice userNotice = userNoticeRepository.findByUserIdAndNoticeId(userId, noticeId)
                .orElseGet(() -> userNoticeRepository.save(UserNotice.create(user, notice)));

        userNotice.toggleScrap();

        return NoticeScrapResponse.of(noticeId, userNotice.getIsScrapped());
    }

    public NoticeScrapListResponse getScrapNotices(Long userId, String categoryTag, String sort, int page, int size) {
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "notice.postedAt");
        if ("deadline".equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Order.asc("notice.deadlineAt").nullsLast());
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Map<String, Long> categoryCounts = buildCategoryCounts(userId);

        Slice<Notice> noticeSlice;
        if (categoryTag == null || categoryTag.isBlank() || "ALL".equalsIgnoreCase(categoryTag)) {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserId(userId, pageable);
        } else {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserIdAndCategoryName(userId, categoryTag, pageable);
        }

        Slice<NoticeSearchItemResponse> responseSlice = noticeSlice.map(NoticeSearchItemResponse::from);

        return NoticeScrapListResponse.of(categoryCounts, responseSlice);
    }

    // 카테고리별 스크랩 개수를 쿼리 1번(GROUP BY)으로 집계
    private Map<String, Long> buildCategoryCounts(Long userId) {
        Map<String, Long> categoryCounts = new HashMap<>();
        long total = 0;
        for (UserNoticeRepository.CategoryCount row : userNoticeRepository.countGroupedByCategoryForUser(userId)) {
            String key = CATEGORY_NAME_TO_COUNT_KEY.get(row.getCategory());
            if (key != null) {
                categoryCounts.put(key, row.getCount());
            }
            total += row.getCount();
        }
        categoryCounts.put("ALL", total);
        return categoryCounts;
    }

    public CalendarDatesResponse getCalendarDates(Long userId, String year, String month) {
        String formattedMonth = month.length() == 1 ? "0" + month : month;
        String yearMonth = year + "-" + formattedMonth;

        List<String> dates = userNoticeRepository.findDistinctDeadlineDatesByUserIdAndYearMonth(userId, yearMonth);
        return CalendarDatesResponse.of(dates);
    }

    public Slice<NoticeSearchItemResponse> getCalendarNotices(Long userId, String date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "notice.deadlineAt"));
        Slice<Notice> noticeSlice = userNoticeRepository.findScrappedNoticesByUserIdAndDeadlineDate(userId, date, pageable);

        return noticeSlice.map(NoticeSearchItemResponse::from);
    }

    public Slice<NoticeSearchItemResponse> getNoDeadlineNotices(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "notice.postedAt"));
        Slice<Notice> noticeSlice = userNoticeRepository.findScrappedNoticesByUserIdAndDeadlineIsNull(userId, pageable);

        return noticeSlice.map(NoticeSearchItemResponse::from);
    }
}