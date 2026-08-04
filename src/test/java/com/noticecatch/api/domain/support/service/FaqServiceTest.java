package com.noticecatch.api.domain.support.service;

import com.noticecatch.api.domain.support.dto.response.FaqResponse;
import com.noticecatch.api.domain.support.entity.Faq;
import com.noticecatch.api.domain.support.repository.FaqRepository;
import com.noticecatch.api.global.apiPayload.response.ListResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    private FaqService faqService;

    @BeforeEach
    void setUp() {
        faqService = new FaqService(faqRepository);
    }

    private Faq faq(Long id, String category) {
        return Faq.builder().id(id).category(category).question("질문").answer("답변").build();
    }

    @Test
    void 카테고리로_FAQ_목록을_최신순으로_조회한다() {
        given(faqRepository.findAllByCategoryOrderByCreatedAtDesc("ACCOUNT"))
                .willReturn(List.of(faq(1L, "ACCOUNT"), faq(2L, "ACCOUNT")));

        ListResponse<FaqResponse> response = faqService.getFaqs("ACCOUNT");

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getContent().get(0).category()).isEqualTo("ACCOUNT");
    }

    @Test
    void 해당_카테고리에_FAQ가_없으면_빈_목록을_반환한다() {
        given(faqRepository.findAllByCategoryOrderByCreatedAtDesc("ETC")).willReturn(List.of());

        ListResponse<FaqResponse> response = faqService.getFaqs("ETC");

        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalCount()).isZero();
    }
}
