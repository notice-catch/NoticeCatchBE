package com.noticecatch.api.domain.department.service;

import com.noticecatch.api.domain.department.dto.response.DepartmentResponse;
import com.noticecatch.api.domain.department.entity.Department;
import com.noticecatch.api.domain.department.exception.DepartmentErrorCode;
import com.noticecatch.api.domain.department.repository.DepartmentRepository;
import com.noticecatch.api.domain.university.entity.University;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private UserRepository userRepository;

    private DepartmentService departmentService;

    @BeforeEach
    void setUp() {
        departmentService = new DepartmentService(departmentRepository, userRepository);
    }

    private Department department(Long id, String name) {
        University university = University.builder().id(1L).name("가천대학교").build();
        return Department.builder().id(id).university(university).name(name).build();
    }

    @Test
    void userId가_null이면_USER_NOT_FOUND를_던진다() {
        assertThatThrownBy(() -> departmentService.getDepartments(null, 1L, null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_유저면_USER_NOT_FOUND를_던진다() {
        given(userRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> departmentService.getDepartments(999L, 1L, null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 대학교에_개설된_학과가_없으면_UNIVERSITY_DEPARTMENT_NOT_FOUND를_던진다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.existsByUniversityId(1L)).willReturn(false);

        assertThatThrownBy(() -> departmentService.getDepartments(1L, 1L, null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(DepartmentErrorCode.UNIVERSITY_DEPARTMENT_NOT_FOUND);
    }

    @Test
    void 검색어가_없으면_대학교_전체_학과목록을_반환한다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.existsByUniversityId(1L)).willReturn(true);
        given(departmentRepository.findByUniversityId(1L))
                .willReturn(List.of(department(1L, "컴퓨터공학과"), department(2L, "전자공학과")));

        ListResponse<DepartmentResponse> response = departmentService.getDepartments(1L, 1L, null);

        assertThat(response.getContent()).hasSize(2);
        verify(departmentRepository, never()).findByUniversityIdAndNameContaining(any(), any());
    }

    @Test
    void 검색어가_있으면_해당_키워드로_학과목록을_검색한다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.existsByUniversityId(1L)).willReturn(true);
        given(departmentRepository.findByUniversityIdAndNameContaining(1L, "컴퓨터"))
                .willReturn(List.of(department(1L, "컴퓨터공학과")));

        ListResponse<DepartmentResponse> response = departmentService.getDepartments(1L, 1L, " 컴퓨터 ");

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).departmentName()).isEqualTo("컴퓨터공학과");
    }

    @Test
    void 검색결과가_없으면_DEPARTMENT_NOT_FOUND_BY_KEYWORD를_던진다() {
        given(userRepository.existsById(1L)).willReturn(true);
        given(departmentRepository.existsByUniversityId(1L)).willReturn(true);
        given(departmentRepository.findByUniversityIdAndNameContaining(1L, "없는학과"))
                .willReturn(List.of());

        assertThatThrownBy(() -> departmentService.getDepartments(1L, 1L, "없는학과"))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(DepartmentErrorCode.DEPARTMENT_NOT_FOUND_BY_KEYWORD);
    }
}
