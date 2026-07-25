package com.noticecatch.api.domain.university.controller;

import com.noticecatch.api.domain.university.dto.response.UniversityResponse;
import com.noticecatch.api.domain.university.service.UniversityService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class UniversityController implements UniversityControllerDocs {

    private final UniversityService universityService;

    @Override
    @GetMapping("/universities")
    public ApiResponse<ListResponse<UniversityResponse>> getUniversities() {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK,
                universityService.getUniversities());
    }

    @Override
    @PatchMapping("/users/university/{universityId}")
    public ApiResponse<Void> selectUniversity(@CurrentUserId Long userId,
                                              @PathVariable Long universityId) {
        universityService.selectUniversity(userId, universityId);
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, null);
    }
}
