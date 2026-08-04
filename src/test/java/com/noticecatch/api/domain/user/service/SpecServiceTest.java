package com.noticecatch.api.domain.user.service;

import com.noticecatch.api.domain.user.dto.request.SpecUpsertRequest;
import com.noticecatch.api.domain.user.dto.response.SpecResponse;
import com.noticecatch.api.domain.user.entity.Spec;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.SpecErrorCode;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.SpecRepository;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.ScrapPageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SpecServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private SpecRepository specRepository;

    private SpecService specService;

    @BeforeEach
    void setUp() {
        specService = new SpecService(userRepository, specRepository);
    }

    private User user(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").build();
    }

    private Spec spec(Long id, User owner) {
        return Spec.builder()
                .id(id).user(owner).category("AWARD").title("수상 제목")
                .organization("주최기관").specDate(LocalDate.of(2026, 1, 1))
                .build();
    }

    private SpecUpsertRequest validRequest() {
        return new SpecUpsertRequest("AWARD", "수상 제목", "주최기관", LocalDate.of(2026, 1, 1), "대상", "메모");
    }

    private Pageable pageable() {
        return PageRequest.of(0, 20);
    }

    private SpecRepository.CategoryCount categoryCount(String category, long count) {
        return new SpecRepository.CategoryCount() {
            @Override
            public String getCategory() {
                return category;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    @Test
    void 카테고리_지정없이_조회하면_전체_스펙목록을_반환한다() {
        User user = user(1L);
        Page<Spec> page = new PageImpl<>(List.of(spec(10L, user)));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(specRepository.findAllByUser(eq(user), any())).willReturn(page);
        given(specRepository.countGroupedByCategory(user)).willReturn(List.of(categoryCount("AWARD", 1L)));

        ScrapPageResponse<SpecResponse> response = specService.getSpecs(1L, "ALL", "latest", pageable());

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getCategoryCounts()).containsEntry("awardCount", 1);
        assertThat(response.getCategoryCounts()).containsEntry("allCount", 1);
        verify(specRepository, never()).findAllByUserAndCategory(any(), any(), any());
    }

    @Test
    void 카테고리를_지정하면_해당_카테고리_스펙목록만_조회한다() {
        User user = user(1L);
        Page<Spec> page = new PageImpl<>(List.of(spec(11L, user)));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(specRepository.findAllByUserAndCategory(eq(user), eq("AWARD"), any())).willReturn(page);
        given(specRepository.countGroupedByCategory(user)).willReturn(List.of());

        ScrapPageResponse<SpecResponse> response = specService.getSpecs(1L, "AWARD", "oldest", pageable());

        assertThat(response.getContent()).hasSize(1);
        verify(specRepository).findAllByUserAndCategory(eq(user), eq("AWARD"), any());
    }

    @Test
    void 존재하지_않는_카테고리로_조회하면_MISSING_REQUIRED_FIELD를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user(1L)));

        assertThatThrownBy(() -> specService.getSpecs(1L, "INVALID", "latest", pageable()))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(SpecErrorCode.MISSING_REQUIRED_FIELD);
    }

    @Test
    void 존재하지_않는_유저가_스펙을_조회하면_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> specService.getSpecs(999L, "ALL", "latest", pageable()))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 필수값이_비어있으면_스펙_추가시_MISSING_REQUIRED_FIELD를_던진다() {
        SpecUpsertRequest request = new SpecUpsertRequest("AWARD", " ", "주최기관", LocalDate.now(), null, null);

        assertThatThrownBy(() -> specService.addSpec(1L, request, pageable()))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(SpecErrorCode.MISSING_REQUIRED_FIELD);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void 정상_요청이면_스펙을_추가하고_최신목록을_반환한다() {
        User user = user(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(specRepository.findAllByUser(eq(user), any())).willReturn(new PageImpl<>(List.of(spec(1L, user))));
        given(specRepository.countGroupedByCategory(user)).willReturn(List.of());

        ScrapPageResponse<SpecResponse> response = specService.addSpec(1L, validRequest(), pageable());

        assertThat(response.getContent()).hasSize(1);
        verify(specRepository).save(any(Spec.class));
    }

    @Test
    void 존재하지_않는_스펙을_수정하면_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(user(1L)));
        given(specRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> specService.updateSpec(1L, 999L, validRequest()))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(SpecErrorCode.NOT_FOUND);
    }

    @Test
    void 다른_유저의_스펙을_수정하면_FORBIDDEN을_던진다() {
        User owner = user(1L);
        User attacker = user(2L);
        Spec spec = spec(10L, owner);
        given(userRepository.findById(2L)).willReturn(Optional.of(attacker));
        given(specRepository.findById(10L)).willReturn(Optional.of(spec));

        assertThatThrownBy(() -> specService.updateSpec(2L, 10L, validRequest()))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(SpecErrorCode.FORBIDDEN);
    }

    @Test
    void 본인_스펙을_수정하면_반영된_목록을_반환한다() {
        User user = user(1L);
        Spec spec = spec(10L, user);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(specRepository.findById(10L)).willReturn(Optional.of(spec));
        given(specRepository.findAllByUser(eq(user), any())).willReturn(new PageImpl<>(List.of(spec)));
        given(specRepository.countGroupedByCategory(user)).willReturn(List.of());

        SpecUpsertRequest updateRequest = new SpecUpsertRequest("LICENSE", "새 제목", "새 기관", LocalDate.of(2026, 2, 2), null, null);
        ScrapPageResponse<SpecResponse> response = specService.updateSpec(1L, 10L, updateRequest);

        assertThat(spec.getTitle()).isEqualTo("새 제목");
        assertThat(spec.getCategory()).isEqualTo("LICENSE");
        assertThat(response.getContent()).hasSize(1);
    }

    @Test
    void 본인_스펙을_삭제하면_삭제후_목록을_반환한다() {
        User user = user(1L);
        Spec spec = spec(10L, user);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(specRepository.findById(10L)).willReturn(Optional.of(spec));
        given(specRepository.findAllByUser(eq(user), any())).willReturn(new PageImpl<>(List.of()));
        given(specRepository.countGroupedByCategory(user)).willReturn(List.of());

        ScrapPageResponse<SpecResponse> response = specService.deleteSpec(1L, 10L);

        verify(specRepository).delete(spec);
        assertThat(response.getContent()).isEmpty();
    }

    @Test
    void 다른_유저의_스펙을_삭제하면_FORBIDDEN을_던진다() {
        User owner = user(1L);
        User attacker = user(2L);
        Spec spec = spec(10L, owner);
        given(userRepository.findById(2L)).willReturn(Optional.of(attacker));
        given(specRepository.findById(10L)).willReturn(Optional.of(spec));

        assertThatThrownBy(() -> specService.deleteSpec(2L, 10L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(SpecErrorCode.FORBIDDEN);

        verify(specRepository, never()).delete(any());
    }
}
