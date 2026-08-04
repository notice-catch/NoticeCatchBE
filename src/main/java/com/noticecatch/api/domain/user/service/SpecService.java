package com.noticecatch.api.domain.user.service;

import com.noticecatch.api.domain.user.dto.request.SpecUpsertRequest;
import com.noticecatch.api.domain.user.dto.response.SpecResponse;
import com.noticecatch.api.domain.user.entity.Spec;
import com.noticecatch.api.domain.user.entity.SpecCategory;
import com.noticecatch.api.domain.user.entity.User;
import com.noticecatch.api.domain.user.exception.SpecErrorCode;
import com.noticecatch.api.domain.user.exception.UserErrorCode;
import com.noticecatch.api.domain.user.repository.SpecRepository;
import com.noticecatch.api.domain.user.repository.UserRepository;
import com.noticecatch.api.global.apiPayload.exception.ProjectException;
import com.noticecatch.api.global.apiPayload.response.ScrapPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecService {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final String ALL_CATEGORY = "ALL";
    private static final String SORT_OLDEST = "oldest";
    private static final String SPEC_DATE_FIELD = "specDate";

    private final UserRepository userRepository;
    private final SpecRepository specRepository;

    public ScrapPageResponse<SpecResponse> getSpecs(Long userId, String category, String sort, Pageable pageable) {
        User user = getUser(userId);
        Pageable sortedPageable = applySort(pageable, sort);
        Page<Spec> specs = isAllCategory(category)
                ? specRepository.findAllByUser(user, sortedPageable)
                : specRepository.findAllByUserAndCategory(user, SpecCategory.from(category).name(), sortedPageable);
        return toResponse(user, specs);
    }

    @Transactional
    public ScrapPageResponse<SpecResponse> addSpec(Long userId, SpecUpsertRequest request, Pageable pageable) {
        validate(request);
        User user = getUser(userId);

        Spec spec = Spec.builder()
                .user(user)
                .category(request.category())
                .title(request.title())
                .organization(request.organization())
                .specDate(request.specDate())
                .scoreOrGrade(request.scoreOrGrade())
                .memo(request.memo())
                .build();
        specRepository.save(spec);

        return toResponse(user, specRepository.findAllByUser(user, applySort(pageable, null)));
    }

    @Transactional
    public ScrapPageResponse<SpecResponse> updateSpec(Long userId, Long specId, SpecUpsertRequest request) {
        validate(request);
        User user = getUser(userId);
        Spec spec = getOwnedSpec(user, specId);

        spec.update(request.category(), request.title(), request.organization(),
                request.specDate(), request.scoreOrGrade(), request.memo());

        return toResponse(user, specRepository.findAllByUser(user, defaultPageable()));
    }

    @Transactional
    public ScrapPageResponse<SpecResponse> deleteSpec(Long userId, Long specId) {
        User user = getUser(userId);
        Spec spec = getOwnedSpec(user, specId);
        specRepository.delete(spec);

        return toResponse(user, specRepository.findAllByUser(user, defaultPageable()));
    }

    private void validate(SpecUpsertRequest request) {
        if (isBlank(request.category()) || isBlank(request.title())
                || isBlank(request.organization()) || request.specDate() == null) {
            throw new ProjectException(SpecErrorCode.MISSING_REQUIRED_FIELD);
        }
        SpecCategory.from(request.category());
    }

    private Spec getOwnedSpec(User user, Long specId) {
        Spec spec = specRepository.findById(specId)
                .orElseThrow(() -> new ProjectException(SpecErrorCode.NOT_FOUND));
        if (!spec.getUser().getId().equals(user.getId())) {
            throw new ProjectException(SpecErrorCode.FORBIDDEN);
        }
        return spec;
    }

    private ScrapPageResponse<SpecResponse> toResponse(User user, Page<Spec> specs) {
        Page<SpecResponse> mapped = specs.map(SpecResponse::from);
        return ScrapPageResponse.of(mapped, buildCategoryCounts(user));
    }

    private Map<String, Integer> buildCategoryCounts(User user) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        counts.put("allCount", 0);
        for (SpecCategory category : SpecCategory.values()) {
            counts.put(category.getCountKey(), 0);
        }

        long total = 0;
        for (SpecRepository.CategoryCount row : specRepository.countGroupedByCategory(user)) {
            SpecCategory category = SpecCategory.from(row.getCategory());
            counts.put(category.getCountKey(), (int) row.getCount());
            total += row.getCount();
        }
        counts.put("allCount", (int) total);
        return counts;
    }

    private boolean isAllCategory(String category) {
        return category == null || category.isBlank() || ALL_CATEGORY.equalsIgnoreCase(category);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private Pageable defaultPageable() {
        return PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE, Sort.by(Sort.Direction.DESC, SPEC_DATE_FIELD));
    }

    private Pageable applySort(Pageable pageable, String sort) {
        Sort.Direction direction = SORT_OLDEST.equalsIgnoreCase(sort) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(direction, SPEC_DATE_FIELD));
    }

    private User getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ProjectException(UserErrorCode.USER_NOT_FOUND));
        if (user.isWithdrawn()) {
            throw new ProjectException(UserErrorCode.USER_NOT_FOUND);
        }
        return user;
    }
}
