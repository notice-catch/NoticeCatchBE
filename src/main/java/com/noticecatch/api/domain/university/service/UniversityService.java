package com.noticecatch.api.domain.university.service;

import com.noticecatch.api.domain.university.dto.response.UniversityResponse;
import com.noticecatch.api.domain.university.entity.University;
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

    public ListResponse<UniversityResponse> getUniversities(Long userId) {
        // 유저 존재 여부 검증
        if (!userRepository.existsById(userId)) {
            throw new ProjectException(GeneralErrorCode.UNAUTHORIZED);
        }

        // 대학 목록 조회
        List<University> universities = universityRepository.findAll();

        // 대학 목록 비어있음 검증
        if (universities.isEmpty()) {
            throw new ProjectException(UniversityErrorCode.UNIVERSITY_LIST_EMPTY);
        }

        // DTO 변환
        List<UniversityResponse> universityResponses = universities.stream()
                .map(UniversityResponse::from)
                .toList();

        return ListResponse.from(universityResponses);
    }

    @Transactional
    public void selectUniversity(Long userId, Long universityId) {
        // 대학교 존재 여부 검증 및 엔티티 조회
        University university = universityRepository.findById(universityId)
                .orElseThrow(() -> new ProjectException(UniversityErrorCode.UNIVERSITY_NOT_FOUND));

        // 유저 존재 여부 검증 및 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(GeneralErrorCode.UNAUTHORIZED));

        // 대학교/학과가 등록되어 있는지 검증
        if (user.getDepartment() != null) { // 또는 user.hasDepartment() 메서드 활용
            throw new ProjectException(UniversityErrorCode.UNIVERSITY_ALREADY_EXISTS);
        }

        user.changeUniversity(university);
    }
}