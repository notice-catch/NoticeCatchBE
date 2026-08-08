package com.noticecatch.api.domain.university.service;

import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.university.dto.response.UniversityResponse;
import com.noticecatch.api.domain.university.entity.University;
import com.noticecatch.api.domain.university.exception.UniversityErrorCode;
import com.noticecatch.api.domain.university.repository.UniversityRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UniversityServiceTest {

    @Mock
    private UniversityRepository universityRepository;
    @Mock
    private UserRepository userRepository;

    private UniversityService universityService;

    @BeforeEach
    void setUp() {
        universityService = new UniversityService(universityRepository, userRepository);
    }

    private University university(Long id) {
        return University.builder().id(id).name("가천대학교").build();
    }

    private User user(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").build();
    }

    @Test
    void 존재하지_않는_유저면_대학목록_조회시_USER_NOT_FOUND를_던진다() {
        given(userRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> universityService.getUniversities(999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 대학목록이_비어있으면_UNIVERSITY_LIST_EMPTY를_던진다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(universityRepository.findAll()).willReturn(List.of());

        assertThatThrownBy(() -> universityService.getUniversities(1L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UniversityErrorCode.UNIVERSITY_LIST_EMPTY);
    }

    @Test
    void 정상_유저면_대학목록을_반환한다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(universityRepository.findAll()).willReturn(List.of(university(1L), university(2L)));

        ListResponse<UniversityResponse> response = universityService.getUniversities(1L);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).universityName()).isEqualTo("가천대학교");
    }

    @Test
    void 존재하지_않는_대학교를_선택하면_UNIVERSITY_NOT_FOUND를_던진다() {
        given(universityRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> universityService.selectUniversity(1L, 999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UniversityErrorCode.UNIVERSITY_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_유저가_대학교를_선택하면_USER_NOT_FOUND를_던진다() {
        given(universityRepository.findById(1L)).willReturn(Optional.of(university(1L)));
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> universityService.selectUniversity(999L, 1L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 이미_학과가_있는_유저가_대학교를_선택하면_UNIVERSITY_ALREADY_EXISTS를_던진다() {
        University university = university(1L);
        Department department = Department.builder().id(1L).university(university).name("컴퓨터공학과").build();
        User user = User.builder().id(1L).email("a@a.com").nickname("유저").department(department).build();

        given(universityRepository.findById(1L)).willReturn(Optional.of(university));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        assertThatThrownBy(() -> universityService.selectUniversity(1L, 1L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UniversityErrorCode.UNIVERSITY_ALREADY_EXISTS);
    }

    @Test
    void 학과가_없는_유저는_대학교를_정상적으로_선택한다() {
        University university = university(1L);
        User user = user(1L);
        given(universityRepository.findById(1L)).willReturn(Optional.of(university));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        universityService.selectUniversity(1L, 1L);

        assertThat(user.getUniversity()).isEqualTo(university);
    }
}
