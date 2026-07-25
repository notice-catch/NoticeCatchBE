package com.noticecatch.api.domain.department.service;

import com.noticecatch.api.domain.department.dto.response.DepartmentResponse;
import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.department.exception.DepartmentErrorCode;
import com.noticecatch.api.domain.department.repository.DepartmentRepository;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public ListResponse<DepartmentResponse> getDepartments(Long userId, Long universityId, String keyword) {
        // 유저 존재 여부 검증 및 엔티티 조회
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ProjectException(UserErrorCode.USER_NOT_FOUND);
        }

        // 해당 대학교에 개설된 학과가 아예 존재하는지 검증
        if (!departmentRepository.existsByUniversityId(universityId)) {
            throw new ProjectException(DepartmentErrorCode.UNIVERSITY_DEPARTMENT_NOT_FOUND);
        }

        List<Department> departments;

        // 검색어 유무에 따른 학과 목록 조회
        if (keyword != null && !keyword.isBlank()) {
            departments = departmentRepository.findByUniversityIdAndNameContaining(universityId, keyword.trim());
            // 검색어 결과가 없는 경우 예외 처리
            if (departments.isEmpty()) {
                throw new ProjectException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND_BY_KEYWORD);
            }
        } else {
            departments = departmentRepository.findByUniversityId(universityId);
        }

        List<DepartmentResponse> responseList = departments.stream()
                .map(DepartmentResponse::from)
                .toList();

        return ListResponse.from(responseList);
    }
}
