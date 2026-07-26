package com.noticecatch.api.global.apiPayload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Slice;
import java.util.List;

@Getter
@AllArgsConstructor
public class SliceResponse<T> {

    private final List<T> content;       // 데이터 목록
    private final int page;              // 현재 페이지 번호
    private final int size;              // 한 페이지당 데이터 개수
    private final boolean hasNext;       // 다음 페이지 존재 여부

    // 💡 Slice로 받아두면 Page 결과도 다 들어올 수 있습니다.
    public static <T> SliceResponse<T> from(Slice<T> slice) {
        return new SliceResponse<>(
                slice.getContent(),
                slice.getNumber(),
                slice.getSize(),
                slice.hasNext()          // Slice 내장 메서드 사용
        );
    }
}