package com.noticecatch.api.domain.user.dto.response;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyPageProfileResponse {
    private final String nickname;
    private final String universityName;
    private final String departmentName;
    private final Integer grade;
    private final long scrapCount;
    private final long keywordCount;
    private final long readCount;

    public static MyPageProfileResponse of(User user, long scrapCount, long keywordCount, long readCount) {
        // 온보딩 중 학과를 아직 선택하지 않은 유저는 department가 null일 수 있음
        Department department = user.getDepartment();
        return new MyPageProfileResponse(
                user.getNickname(),
                department != null ? department.getUniversity().getName() : null,
                department != null ? department.getName() : null,
                user.getGrade(),
                scrapCount,
                keywordCount,
                readCount
        );
    }
}
