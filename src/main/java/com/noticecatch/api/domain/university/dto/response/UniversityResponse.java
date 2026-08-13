package com.noticecatch.api.domain.university.dto.response;

import com.noticecatch.api.domain.university.entity.University;
import lombok.Builder;

@Builder
public record UniversityResponse(
        Long universityId,
        String universityName
) {
    public static UniversityResponse from(University university) {
        return UniversityResponse.builder()
                .universityId(university.getId())
                .universityName(university.getName())
                .build();
    }
}