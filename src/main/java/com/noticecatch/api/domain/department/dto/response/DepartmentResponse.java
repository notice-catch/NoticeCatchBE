package com.noticecatch.api.domain.department.dto.response;

import com.noticecatch.api.domain.department.entity.Department;

public record DepartmentResponse(
        Long departmentId,
        String departmentName
) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName()
        );
    }
}
