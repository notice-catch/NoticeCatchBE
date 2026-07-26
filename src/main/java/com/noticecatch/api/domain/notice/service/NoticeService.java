package com.noticecatch.api.domain.notice.service;

import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeSearchItemResponse;
import com.noticecatch.api.domain.notice.dto.response.NoticeSearchListResponse;
import com.noticecatch.api.domain.notice.entity.Notice;
import com.noticecatch.api.domain.notice.entity.UserNotice;
import com.noticecatch.api.domain.notice.exception.NoticeErrorCode;
import com.noticecatch.api.domain.notice.repository.NoticeRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final UserNoticeRepository userNoticeRepository;

    public NoticeDetailResponse getNoticeDetail(Long userId, Long noticeId) {
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ProjectException(NoticeErrorCode.NOTICE_NOT_FOUND));

        boolean isScrapped = false;
        if (userId != null) {
            isScrapped = userNoticeRepository.findByUserIdAndNoticeId(userId, noticeId)
                    .map(UserNotice::getIsScrapped)
                    .orElse(false);
        }

        return NoticeDetailResponse.of(notice, isScrapped);
    }

    public NoticeSearchListResponse searchNotices(String searchWord, String sort, int page, int size) {
        if (searchWord == null || searchWord.trim().length() < 2) {
            throw new ProjectException(NoticeErrorCode.SEARCH_KEYWORD_TOO_SHORT);
        }

        String trimmedKeyword = searchWord.trim();
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "postedAt");

        if ("deadline".equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Order.asc("deadlineAt").nullsLast());
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<Notice> noticeSlice = noticeRepository.searchByKeyword(trimmedKeyword, pageable);

        Slice<NoticeSearchItemResponse> responseSlice = noticeSlice.map(NoticeSearchItemResponse::from);

        return NoticeSearchListResponse.of(responseSlice);
    }

    public Slice<NoticeSearchItemResponse> getNotices(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt"));

        Slice<Notice> noticeSlice;
        if (keyword != null && !keyword.isBlank()) {
            noticeSlice = noticeRepository.findByCategory_Name(keyword.trim(), pageable);
        } else {
            noticeSlice = noticeRepository.findAll(pageable);
        }

        return noticeSlice.map(NoticeSearchItemResponse::from);
    }

    public List<String> getCalendarDates(String year, String month) {
        String formattedMonth = month.length() == 1 ? "0" + month : month;
        String yearMonth = year + "-" + formattedMonth;

        return noticeRepository.findDistinctDeadlineDatesByYearMonth(yearMonth);
    }

    public Slice<NoticeSearchItemResponse> getCalendarNotices(String date, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "deadlineAt"));
        Slice<Notice> noticeSlice = noticeRepository.findByDeadlineAtDate(date, pageable);

        return noticeSlice.map(NoticeSearchItemResponse::from);
    }

    public Slice<NoticeSearchItemResponse> getNoDeadlineNotices(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postedAt"));
        Slice<Notice> noticeSlice = noticeRepository.findByDeadlineAtIsNull(pageable);

        return noticeSlice.map(NoticeSearchItemResponse::from);
    }
}