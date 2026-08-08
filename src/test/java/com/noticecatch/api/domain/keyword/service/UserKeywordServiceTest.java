package com.noticecatch.api.domain.keyword.service;

import com.noticecatch.api.domain.keyword.dto.request.UserKeywordUpdateRequest;
import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.keyword.exception.KeywordErrorCode;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.code.GeneralErrorCode;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserKeywordServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserKeywordRepository userKeywordRepository;

    private UserKeywordService userKeywordService;

    @BeforeEach
    void setUp() {
        userKeywordService = new UserKeywordService(userRepository, userKeywordRepository);
    }

    private User user(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").build();
    }

    private User withdrawnUser(Long id) {
        return User.builder().id(id).email("a@a.com").nickname("유저").status("WITHDRAWN").build();
    }

    private UserKeywordUpdateRequest.KeywordItem item(String keyword, String type) {
        return new UserKeywordUpdateRequest.KeywordItem(keyword, type);
    }

    @Test
    void userId가_null이면_추천키워드_조회시_USER_NOT_FOUND를_던진다() {
        assertThatThrownBy(() -> userKeywordService.getRecommendKeywords(null))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_유저면_추천키워드_조회시_USER_NOT_FOUND를_던진다() {
        given(userRepository.existsById(999L)).willReturn(false);

        assertThatThrownBy(() -> userKeywordService.getRecommendKeywords(999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 정상_유저면_추천키워드_목록을_반환한다() {
        given(userRepository.existsById(1L)).willReturn(true);

        ListResponse<UserKeywordResponse> response = userKeywordService.getRecommendKeywords(1L);

        assertThat(response.getContent()).isNotEmpty();
        assertThat(response.getTotalCount()).isEqualTo(response.getContent().size());
    }

    @Test
    void 유저의_관심키워드_목록을_조회한다() {
        User user = user(1L);
        UserKeyword keyword = UserKeyword.builder().user(user).keyword("장학금").keywordType("CUSTOM").build();
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userKeywordRepository.findAllByUser(user)).willReturn(List.of(keyword));

        ListResponse<UserKeywordResponse> response = userKeywordService.getUserKeywords(1L);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).keyword()).isEqualTo("장학금");
    }

    @Test
    void 존재하지_않는_유저의_관심키워드_조회시_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userKeywordService.getUserKeywords(999L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);
    }

    @Test
    void 탈퇴한_유저의_관심키워드_조회시_USER_NOT_FOUND를_던진다() {
        given(userRepository.findById(1L)).willReturn(Optional.of(withdrawnUser(1L)));

        assertThatThrownBy(() -> userKeywordService.getUserKeywords(1L))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(UserErrorCode.USER_NOT_FOUND);

        verify(userKeywordRepository, never()).findAllByUser(any());
    }

    @Test
    void 키워드_목록이_비어있으면_BAD_REQUEST를_던진다() {
        UserKeywordUpdateRequest request = new UserKeywordUpdateRequest(List.of());

        assertThatThrownBy(() -> userKeywordService.updateUserKeywords(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(GeneralErrorCode.BAD_REQUEST);

        verify(userRepository, never()).findById(any());
    }

    @Test
    void 키워드가_10개를_초과하면_MAX_COUNT_EXCEEDED를_던진다() {
        List<UserKeywordUpdateRequest.KeywordItem> items = java.util.stream.IntStream.range(0, 11)
                .mapToObj(i -> item("키워드" + i, "CUSTOM"))
                .toList();
        UserKeywordUpdateRequest request = new UserKeywordUpdateRequest(items);

        assertThatThrownBy(() -> userKeywordService.updateUserKeywords(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(KeywordErrorCode.MAX_COUNT_EXCEEDED);
    }

    @Test
    void 직접입력_키워드가_10자를_초과하면_CUSTOM_LENGTH_EXCEEDED를_던진다() {
        UserKeywordUpdateRequest request = new UserKeywordUpdateRequest(
                List.of(item("아주아주아주아주긴키워드입니다", "CUSTOM")));

        assertThatThrownBy(() -> userKeywordService.updateUserKeywords(1L, request))
                .isInstanceOf(ProjectException.class)
                .extracting(e -> ((ProjectException) e).getErrorCode())
                .isEqualTo(KeywordErrorCode.CUSTOM_LENGTH_EXCEEDED);
    }

    @Test
    void 정상_요청이면_기존_키워드를_삭제하고_새로_저장한다() {
        User user = user(1L);
        UserKeywordUpdateRequest request = new UserKeywordUpdateRequest(
                List.of(item("장학금", "CUSTOM"), item("공모전", "RECOMMEND")));
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        ListResponse<UserKeywordResponse> response = userKeywordService.updateUserKeywords(1L, request);

        assertThat(response.getContent()).hasSize(2);
        verify(userKeywordRepository).deleteAllByUser(user);
        verify(userKeywordRepository).saveAll(anyList());
    }
}
