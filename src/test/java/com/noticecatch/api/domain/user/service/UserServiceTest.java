package com.noticecatch.api.domain.user.service;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.department.exception.DepartmentErrorCode;
import com.noticecatch.api.domain.department.repository.DepartmentRepository;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.notice.repository.UserNoticeRepository;
import com.noticecatch.api.domain.university.entity.University;
import com.noticecatch.api.domain.user.dto.request.AlarmUpdateRequest;
import com.noticecatch.api.domain.user.dto.request.ProfileUpdateRequest;
import com.noticecatch.api.domain.user.dto.response.AlarmSettingResponse;
import com.noticecatch.api.domain.user.dto.response.MyPageProfileResponse;
import com.noticecatch.api.domain.user.dto.response.ProfileResponse;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserNoticeRepository userNoticeRepository;
    @Mock
    private UserKeywordRepository userKeywordRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, departmentRepository, userNoticeRepository, userKeywordRepository);
    }

    private University university(Long id) {
        return University.builder().id(id).name("가천대학교").build();
    }

    private Department department(Long id, University university) {
        return Department.builder().id(id).university(university).name("컴퓨터공학과").build();
    }

    private User activeUser(Long id) {
        return User.builder()
                .id(id).email("a@a.com").nickname("유저").status("ACTIVE")
                .allNotification(true).closingNotification(true).keywordNotification(true)
                .scholarship(true).extracurricular(true).academic(true).employment(true)
                .build();
    }

    private User withdrawnUser(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").status("WITHDRAWN").build();
    }

    @Test
    void 온보딩_프로필_수정시_학과와_학년을_반영한다() {
        User user = activeUser(1L);
        University univ = university(1L);
        Department dept = department(2L, univ);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(departmentRepository.findById(2L)).willReturn(Optional.of(dept));

        userService.updateOnboardingProfile(1L, 2L, new ProfileUpdateRequest(3));

        assertThat(user.getDepartment()).isEqualTo(dept);
        assertThat(user.getGrade()).isEqualTo(3);
    }

    @Test
    void 온보딩_프로필_수정시_존재하지_않는_유저면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateOnboardingProfile(999L, 2L, new ProfileUpdateRequest(1)))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 온보딩_프로필_수정시_탈퇴한_유저면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(withdrawnUser(1L)));

        assertThatThrownBy(() -> userService.updateOnboardingProfile(1L, 2L, new ProfileUpdateRequest(1)))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 온보딩_프로필_수정시_존재하지_않는_학과면_DEPARTMENT_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(departmentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateOnboardingProfile(1L, 999L, new ProfileUpdateRequest(1)))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
    }

    @Test
    void 마이페이지_조회시_스크랩_읽음_키워드_개수를_함께_반환한다() {
        University univ = university(1L);
        Department dept = department(1L, univ);
        User user = activeUser(1L);
        user.changeDepartment(dept);

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userNoticeRepository.countByUserAndIsScrapped(user, true)).willReturn(5L);
        given(userNoticeRepository.countByUserAndIsRead(user, true)).willReturn(7L);
        given(userKeywordRepository.countByUser(user)).willReturn(3L);

        MyPageProfileResponse response = userService.getMyPage(1L);

        assertThat(response.getScrapCount()).isEqualTo(5L);
        assertThat(response.getReadCount()).isEqualTo(7L);
        assertThat(response.getKeywordCount()).isEqualTo(3L);
        assertThat(response.getUniversityName()).isEqualTo("가천대학교");
    }

    @Test
    void 마이페이지_조회시_존재하지_않는_유저면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getMyPage(999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 프로필_수정시_학과가_선택한_대학교에_속하지_않으면_예외를_던진다() {
        University univ1 = university(1L);
        University univ2 = university(2L);
        Department dept = department(1L, univ2);
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));

        assertThatThrownBy(() -> userService.updateProfile(1L, univ1.getId(), 1L, new ProfileUpdateRequest(2)))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_UNIVERSITY_OR_DEPARTMENT);
    }

    @Test
    void 프로필_수정시_존재하지_않는_학과면_INVALID_UNIVERSITY_OR_DEPARTMENT를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(departmentRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateProfile(1L, 1L, 999L, new ProfileUpdateRequest(2)))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.INVALID_UNIVERSITY_OR_DEPARTMENT);
    }

    @Test
    void 정상_프로필_수정시_학과와_학년이_반영된_응답을_반환한다() {
        University univ = university(1L);
        Department dept = department(1L, univ);
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        given(departmentRepository.findById(1L)).willReturn(Optional.of(dept));

        ProfileResponse response = userService.updateProfile(1L, 1L, 1L, new ProfileUpdateRequest(4));

        assertThat(response.getDepartmentName()).isEqualTo("컴퓨터공학과");
        assertThat(response.getGrade()).isEqualTo(4);
    }

    @Test
    void 알림설정_조회시_현재_유저의_설정값을_반환한다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));

        AlarmSettingResponse response = userService.getAlarmSettings(1L);

        assertThat(response.isAll()).isTrue();
        assertThat(response.isClosing()).isTrue();
    }

    @Test
    void 알림설정_수정시_요청값이_모두_비어있으면_ALARM_SETTING_REQUIRED를_던진다() {
        AlarmUpdateRequest request = new AlarmUpdateRequest(null, null, null, null, null, null, null);

        assertThatThrownBy(() -> userService.updateAlarmSettings(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.ALARM_SETTING_REQUIRED);
    }

    @Test
    void 전체알림을_끄면서_하위알림을_켜두면_ALARM_SUB_SETTING_CONFLICT를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        AlarmUpdateRequest request = new AlarmUpdateRequest(false, null, null, null, null, null, null);

        assertThatThrownBy(() -> userService.updateAlarmSettings(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.ALARM_SUB_SETTING_CONFLICT);
    }

    @Test
    void 전체알림을_끄면서_하위알림도_모두_끄면_정상_반영된다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        AlarmUpdateRequest request = new AlarmUpdateRequest(false, false, false, false, false, false, false);

        AlarmSettingResponse response = userService.updateAlarmSettings(1L, request);

        assertThat(response.isAll()).isFalse();
        assertThat(response.isClosing()).isFalse();
    }

    @Test
    void 일부_알림설정만_수정하면_나머지값은_기존값을_유지한다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(activeUser(1L)));
        AlarmUpdateRequest request = new AlarmUpdateRequest(null, null, false, null, null, null, null);

        AlarmSettingResponse response = userService.updateAlarmSettings(1L, request);

        assertThat(response.isAll()).isTrue();
        assertThat(response.isKeyword()).isFalse();
        assertThat(response.isClosing()).isTrue();
    }
}
