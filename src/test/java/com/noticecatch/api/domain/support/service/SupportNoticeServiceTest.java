package com.noticecatch.api.domain.support.service;

import com.noticecatch.api.domain.support.dto.response.SupportNoticeResponse;
import com.noticecatch.api.domain.support.entity.SupportNotice;
import com.noticecatch.api.domain.support.repository.SupportNoticeRepository;
import com.noticecatch.api.global.apiPayload.response.SliceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class SupportNoticeServiceTest {

    @Mock
    private SupportNoticeRepository supportNoticeRepository;

    private SupportNoticeService supportNoticeService;

    @BeforeEach
    void setUp() {
        supportNoticeService = new SupportNoticeService(supportNoticeRepository);
    }

    private SupportNotice supportNotice(Long id) {
        return SupportNotice.builder().id(id).title("공지 제목").content("공지 내용").build();
    }

    @Test
    void 고객센터_공지목록을_최신순으로_조회한다() {
        Pageable pageable = PageRequest.of(0, 10);
        given(supportNoticeRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(supportNotice(1L), supportNotice(2L)), pageable, 2));

        SliceResponse<SupportNoticeResponse> response = supportNoticeService.getSupportNotices(pageable);

        assertThat(response.getContent()).hasSize(2);
        assertThat(response.getContent().get(0).title()).isEqualTo("공지 제목");
    }

    @Test
    void 고객센터_공지가_없으면_빈_목록을_반환한다() {
        Pageable pageable = PageRequest.of(0, 10);
        given(supportNoticeRepository.findAllByOrderByCreatedAtDesc(any()))
                .willReturn(new PageImpl<>(List.of(), pageable, 0));

        SliceResponse<SupportNoticeResponse> response = supportNoticeService.getSupportNotices(pageable);

        assertThat(response.getContent()).isEmpty();
        assertThat(response.isHasNext()).isFalse();
    }
}
