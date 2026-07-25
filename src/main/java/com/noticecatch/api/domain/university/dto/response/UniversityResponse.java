package com.noticecatch.api.domain.university.dto.response;

import com.noticecatch.api.domain.university.entity.University;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UniversityResponse {

    private Long universityId;
    private String universityName;

    public static UniversityResponse from(University university) {
        return UniversityResponse.builder()
                .universityId(university.getId())
                .universityName(university.getName())
                .build();
    }
}