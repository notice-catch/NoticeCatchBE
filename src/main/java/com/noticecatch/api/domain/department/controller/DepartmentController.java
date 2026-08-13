package com.noticecatch.api.domain.department.controller;

import com.noticecatch.api.domain.department.dto.response.DepartmentResponse;
import com.noticecatch.api.domain.department.service.DepartmentService;
import com.noticecatch.api.global.apiPayload.ApiResponse;
import com.noticecatch.api.global.apiPayload.code.GeneralSuccessCode;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import com.noticecatch.api.global.resolver.CurrentUserId;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class DepartmentController implements DepartmentControllerDocs {
    private final DepartmentService departmentService;

    @Override
    @GetMapping("/universities/{universityId}/departments")
    public ApiResponse<ListResponse<DepartmentResponse>> getDepartments(
            @CurrentUserId Long userId,
            @PathVariable Long universityId,
            @RequestParam(name = "keyword", required = false) String keyword) {
        return ApiResponse.onSuccess(GeneralSuccessCode.OK, departmentService.getDepartments(
                userId, universityId, keyword));
    }
}