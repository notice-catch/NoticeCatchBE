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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
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
        // 유저 존재 및 탈퇴 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        if (user.isWithdrawn()) {
            throw new ProjectException(UserErrorCode.USER_NOT_FOUND);
        }

        // 공지 존재 여부 검증
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> new ProjectException(NoticeErrorCode.NOTICE_NOT_FOUND));

        // 스크랩 레코드 조회, 없으면 최초 생성
        UserNotice userNotice = findOrCreateUserNotice(userId, user, notice);

        userNotice.toggleScrap();

        return NoticeScrapResponse.of(noticeId, userNotice.getIsScrapped());
    }

    // user_notices(user_id, notice_id) 유니크 제약을 신뢰해 동시 스크랩 요청의 경쟁 상태를 처리한다.
    // 두 요청이 동시에 "없음"을 보고 둘 다 저장을 시도하면 나중 저장은 제약 위반으로 실패하는데,
    // 그 경우 예외를 삼키지 않고 먼저 커밋된 행을 다시 조회해 그 행에 이어서 토글한다.
    private UserNotice findOrCreateUserNotice(Long userId, User user, Notice notice) {
        return userNoticeRepository.findByUserIdAndNoticeId(userId, notice.getId())
                .orElseGet(() -> {
                    try {
                        return userNoticeRepository.saveAndFlush(UserNotice.create(user, notice));
                    } catch (DataIntegrityViolationException e) {
                        return userNoticeRepository.findByUserIdAndNoticeId(userId, notice.getId())
                                .orElseThrow(() -> e);
                    }
                });
    }

    public NoticeScrapListResponse getScrapNotices(Long userId, String categoryTag, String sort, int page, int size) {
        // 정렬 조건 설정
        Sort sortOrder = Sort.by(Sort.Direction.DESC, "notice.postedAt");
        if ("deadline".equalsIgnoreCase(sort)) {
            sortOrder = Sort.by(Sort.Order.asc("notice.deadlineAt").nullsLast());
        }
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 카테고리별 스크랩 개수 집계
        Map<String, Long> categoryCounts = buildCategoryCounts(userId);

        // 카테고리 필터 여부에 따라 스크랩 목록 조회
        Slice<Notice> noticeSlice;
        if (categoryTag == null || categoryTag.isBlank() || "ALL".equalsIgnoreCase(categoryTag)) {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserId(userId, pageable);
        } else {
            noticeSlice = userNoticeRepository.findScrappedNoticesByUserIdAndCategoryName(userId, categoryTag, pageable);
        }

        // DTO 변환
        Slice<NoticeSearchItemResponse> responseSlice = noticeSlice.map(NoticeSearchItemResponse::from);

        return NoticeScrapListResponse.of(categoryCounts, responseSlice);
    }

    // 카테고리별 스크랩 개수를 쿼리 1번(GROUP BY)으로 집계
    private Map<String, Long> buildCategoryCounts(Long userId) {
        Map<String, Long> categoryCounts = new LinkedHashMap<>();
        categoryCounts.put("ALL", 0L);
        for (String key : CATEGORY_NAME_TO_COUNT_KEY.values()) {
            categoryCounts.put(key, 0L);
        }

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