package com.noticecatch.api.domain.support.controller;

import com.noticecatch.api.domain.support.dto.response.FaqResponse;
import com.noticecatch.api.domain.support.dto.response.SupportNoticeResponse;
import com.noticecatch.api.domain.support.service.FaqService;
import com.noticecatch.api.domain.support.service.SupportNoticeService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/support")
public class SupportController implements SupportControllerDocs {

    private final SupportNoticeService supportNoticeService;
    private final FaqService faqService;

    @Override
    @GetMapping("/notices")
    public ApiResponse<SliceResponse<SupportNoticeResponse>> getSupportNotices(
            @RequestParam int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                supportNoticeService.getSupportNotices(PageRequest.of(page, size)));
    }

    @Override
    @GetMapping("/faqs")
    public ApiResponse<ListResponse<FaqResponse>> getFaqs(@RequestParam String category) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, faqService.getFaqs(category));
    }
}
