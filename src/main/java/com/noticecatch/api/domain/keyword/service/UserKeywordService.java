package com.noticecatch.api.domain.keyword.service;

import com.noticecatch.api.domain.keyword.dto.request.UserKeywordUpdateRequest;
import com.noticecatch.api.domain.keyword.dto.response.UserKeywordResponse;
import com.noticecatch.api.domain.keyword.entity.RecommendKeyword;
import com.noticecatch.api.domain.keyword.entity.UserKeyword;
import com.noticecatch.api.domain.keyword.exception.KeywordErrorCode;
import com.noticecatch.api.domain.keyword.repository.UserKeywordRepository;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
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
public class UserKeywordService {

    private static final int MAX_KEYWORD_COUNT = 10;
    private static final int MAX_CUSTOM_KEYWORD_LENGTH = 10;
    private static final String CUSTOM_KEYWORD_TYPE = "CUSTOM";

    private final UserRepository userRepository;
    private final UserKeywordRepository userKeywordRepository;

    @Transactional(readOnly = true)
    public ListResponse<UserKeywordResponse> getRecommendKeywords(Long userId) {
        // 유저 존재 여부 검증 및 엔티티 조회
        if (userId == null || !userRepository.existsById(userId)) {
            throw new ProjectException(UserErrorCode.USER_NOT_FOUND);
        }

        // 추천 키워드 목록 조회
        List<UserKeywordResponse> recommendList = RecommendKeyword.getRecommendList();

        return ListResponse.from(recommendList);
    }

    public ListResponse<UserKeywordResponse> getUserKeywords(Long userId) {
        User user = getUser(userId);
        List<UserKeywordResponse> responses = userKeywordRepository.findAllByUser(user).stream()
                .map(UserKeywordResponse::from)
                .toList();
        return ListResponse.from(responses);
    }

    @Transactional
    public ListResponse<UserKeywordResponse> updateUserKeywords(Long userId, UserKeywordUpdateRequest request) {
        if (request.isEmpty()) {
            throw new ProjectException(GeneralErrorCode.BAD_REQUEST);
        }
        if (request.keywords().size() > MAX_KEYWORD_COUNT) {
            throw new ProjectException(KeywordErrorCode.MAX_COUNT_EXCEEDED);
        }
        for (UserKeywordUpdateRequest.KeywordItem item : request.keywords()) {
            if (CUSTOM_KEYWORD_TYPE.equalsIgnoreCase(item.keywordType())
                    && item.keyword() != null && item.keyword().length() > MAX_CUSTOM_KEYWORD_LENGTH) {
                throw new ProjectException(KeywordErrorCode.CUSTOM_LENGTH_EXCEEDED);
            }
        }

        User user = getUser(userId);
        userKeywordRepository.deleteAllByUser(user);

        List<UserKeyword> newKeywords = request.keywords().stream()
                .map(item -> UserKeyword.builder()
                        .user(user)
                        .keyword(item.keyword())
                        .keywordType(item.keywordType())
                        .build())
                .toList();
        userKeywordRepository.saveAll(newKeywords);

        List<UserKeywordResponse> responses = newKeywords.stream()
                .map(UserKeywordResponse::from)
                .toList();
        return ListResponse.from(responses);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
    }
}
