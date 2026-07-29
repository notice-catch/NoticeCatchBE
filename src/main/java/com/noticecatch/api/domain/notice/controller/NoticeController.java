package com.noticecatch.api.domain.notice.controller;

import com.noticecatch.api.domain.notice.dto.response.*;
import com.noticecatch.api.domain.notice.service.NoticeService;
import com.noticecatch.api.domain.notice.service.UserNoticeService;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController implements NoticeControllerDocs {

    private final NoticeService noticeService;
    private final UserNoticeService userNoticeService;

    @Override
    @GetMapping
    public ApiResponse<SliceResponse<NoticeSearchItemResponse>> getNotices(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                noticeService.getNotices(userId, page, size, keyword));
    }

    @Override
    @GetMapping("/{noticeId}")
    public ApiResponse<NoticeDetailResponse> getNoticeDetail(
            @CurrentUserId Long userId,
            @PathVariable Long noticeId) {
        NoticeDetailResponse response = noticeService.getNoticeDetail(userId, noticeId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/search")
    public ApiResponse<NoticeSearchListResponse> searchNotices(
            @RequestParam String searchWord,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        NoticeSearchListResponse response = noticeService.searchNotices(searchWord, sort, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @PostMapping("/{noticeId}/scrap")
    public ApiResponse<NoticeScrapResponse> scrapNotice(
            @CurrentUserId Long userId,
            @PathVariable Long noticeId) {
        NoticeScrapResponse response = userNoticeService.scrapNotice(userId, noticeId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/scraps")
    public ApiResponse<NoticeScrapListResponse> getScrapNotices(
            @CurrentUserId Long userId,
            @RequestParam(required = false) String categoryTag,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        NoticeScrapListResponse response = userNoticeService.getScrapNotices(userId, categoryTag, sort, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/calendar/dates")
    public ApiResponse<CalendarDatesResponse> getCalendarDates(
            @CurrentUserId Long userId,
            @RequestParam String year,
            @RequestParam String month) {
        CalendarDatesResponse response = userNoticeService.getCalendarDates(userId, year, month);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, response);
    }

    @Override
    @GetMapping("/calendar")
    public ApiResponse<SliceResponse<NoticeSearchItemResponse>> getCalendarNotices(
            @CurrentUserId Long userId,
            @RequestParam String date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Slice<NoticeSearchItemResponse> response = userNoticeService.getCalendarNotices(userId, date, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, SliceResponse.from(response));
    }

    @Override
    @GetMapping("/calendar/no-deadline")
    public ApiResponse<SliceResponse<NoticeSearchItemResponse>> getNoDeadlineNotices(
            @CurrentUserId Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Slice<NoticeSearchItemResponse> response = userNoticeService.getNoDeadlineNotices(userId, page, size);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, SliceResponse.from(response));
    }
}