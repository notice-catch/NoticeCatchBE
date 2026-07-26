package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.*;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.exception.NoticeErrorCode;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.code.GeneralErrorCode;
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

    private final UserNoticeRepository userNoticeRepository;
    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Transactional
    public NoticeScrapResponse scrapNotice(Long userId, Long noticeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.NOT_FOUND));

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

        Map<String, Long> categoryCounts = new HashMap<>();
        categoryCounts.put("ALL", userNoticeRepository.countByUserIdAndIsScrappedTrue(userId));
        categoryCounts.put("SCHOLARSHIP", userNoticeRepository.countByUserIdAndCategoryNameAndIsScrappedTrue(userId, "장학"));
        categoryCounts.put("ACADEMIC", userNoticeRepository.countByUserIdAndCategoryNameAndIsScrappedTrue(userId, "학사"));
        categoryCounts.put("EMPLOYMENT", userNoticeRepository.countByUserIdAndCategoryNameAndIsScrappedTrue(userId, "취업"));

        Slice<Notice> noticeSlice;
        if (categoryTag == null || categoryTag.isBlank() || "ALL".equalsIgnoreCase(categoryTag)) {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserId(userId, pageable);
        } else {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserIdAndCategoryName(userId, categoryTag, pageable);
        }

        Slice<NoticeSearchItemResponse> responseSlice = noticeSlice.map(NoticeSearchItemResponse::from);

        return NoticeScrapListResponse.of(categoryCounts, responseSlice);
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