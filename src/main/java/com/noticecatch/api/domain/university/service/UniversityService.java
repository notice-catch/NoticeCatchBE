package com.noticecatch.api.domain.university.service;

import com.noticecatch.api.domain.university.dto.response.UniversityResponse;
import com.noticecatch.api.domain.university.exception.UniversityErrorCode;
import com.noticecatch.api.domain.university.repository.UniversityRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.code.GeneralErrorCode;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UniversityService {

    private final UniversityRepository universityRepository;
    private final UserRepository userRepository;

    public ListResponse<UniversityResponse> getUniversities() {
        List<UniversityResponse> universityDtos = universityRepository.findAll().stream()
                .map(UniversityResponse::from)
                .toList();

        // 대학 정보가 없는 경우 예외 응답
        if (universityDtos.isEmpty()) {
            throw new ProjectException(UniversityErrorCode.UNIVERSITY_LIST_EMPTY);
        }

        return ListResponse.from(universityDtos);
    }

    @Transactional
    public void selectUniversity(Long userId, Long universityId) {
        // 대학교 존재 여부 검증
        boolean exists = universityRepository.existsById(universityId);
        if (!exists) {
            throw new ProjectException(UniversityErrorCode.UNIVERSITY_NOT_FOUND);
        }

        // 유저 존재 여부 검증
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.UNAUTHORIZED));

        // 이미 대학교/학과가 등록되어 있는지 검증
        if (user.getDepartment() != null) {
            throw new ProjectException(UniversityErrorCode.UNIVERSITY_ALREADY_EXISTS);
        }
    }
}
