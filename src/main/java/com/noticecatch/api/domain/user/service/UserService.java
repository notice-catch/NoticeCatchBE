package com.noticecatch.api.domain.user.service;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.department.exception.DepartmentErrorCode;
import com.noticecatch.api.domain.department.repository.DepartmentRepository;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.user.dto.request.AlarmUpdateRequest;
import com.noticecatch.api.domain.user.dto.request.ProfileUpdateRequest;
import com.noticecatch.api.domain.user.dto.response.AlarmSettingResponse;
import com.noticecatch.api.domain.user.dto.response.MyPageProfileResponse;
import com.noticecatch.api.domain.user.dto.response.ProfileResponse;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final UserNoticeRepository userNoticeRepository;
    private final UserKeywordRepository userKeywordRepository;

    @Transactional
    public void updateOnboardingProfile(Long userId, Long departmentId, ProfileUpdateRequest request) {
        // 유저 존재 여부 검증 (탈퇴 여부 포함)
        User user = getActiveUser(userId);

        // 학과 존재 여부 검증
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ProjectException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND));

        // 학과 변경 및 학년 업데이트
        user.changeDepartment(department);
        user.updateProfile(user.getNickname(), request.grade());
    }

    public MyPageProfileResponse getMyPage(Long userId) {
        User user = getActiveUser(userId);
        long scrapCount = userNoticeRepository.countByUserAndIsScrapped(user, true);
        long readCount = userNoticeRepository.countByUserAndIsRead(user, true);
        long keywordCount = userKeywordRepository.countByUser(user);
        return MyPageProfileResponse.of(user, scrapCount, keywordCount, readCount);
    }

    @Transactional
    public ProfileResponse updateProfile(Long userId, Long universityId, Long departmentId, ProfileUpdateRequest request) {
        User user = getActiveUser(userId);
        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.INVALID_UNIVERSITY_OR_DEPARTMENT));
        if (!department.getUniversity().getId().equals(universityId)) {
            throw new ProjectException(UserErrorCode.INVALID_UNIVERSITY_OR_DEPARTMENT);
        }

        user.changeDepartment(department);
        user.updateProfile(user.getNickname(), request.grade());
        return ProfileResponse.from(user);
    }

    public AlarmSettingResponse getAlarmSettings(Long userId) {
        User user = getActiveUser(userId);
        return AlarmSettingResponse.from(user);
    }

    @Transactional
    public AlarmSettingResponse updateAlarmSettings(Long userId, AlarmUpdateRequest request) {
        if (request.isEmpty()) {
            throw new ProjectException(UserErrorCode.ALARM_SETTING_REQUIRED);
        }
        User user = getActiveUser(userId);

        boolean isAll = orElseCurrent(request.isAll(), user.getAllNotification());
        boolean isClosing = orElseCurrent(request.isClosing(), user.getClosingNotification());
        boolean isKeyword = orElseCurrent(request.isKeyword(), user.getKeywordNotification());
        boolean scholarship = orElseCurrent(request.scholarship(), user.getScholarship());
        boolean extracurricular = orElseCurrent(request.extracurricular(), user.getExtracurricular());
        boolean academic = orElseCurrent(request.academic(), user.getAcademic());
        boolean employment = orElseCurrent(request.employment(), user.getEmployment());

        if (!isAll && (isClosing || isKeyword || scholarship || extracurricular || academic || employment)) {
            throw new ProjectException(UserErrorCode.ALARM_SUB_SETTING_CONFLICT);
        }

        user.updateAlarmSettings(isAll, isClosing, isKeyword, scholarship, extracurricular, academic, employment);
        return AlarmSettingResponse.from(user);
    }

    private boolean orElseCurrent(Boolean requested, Boolean current) {
        return requested != null ? requested : Boolean.TRUE.equals(current);
    }

    private User getActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        if (user.isWithdrawn()) {
            throw new ProjectException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
