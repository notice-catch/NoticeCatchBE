package com.noticecatch.api.domain.notice.exception;

// AI 요약 생성 실패를 배치 호출부에 알리기 위한 내부 전용 예외 — API 응답으로 노출되지 않고 배치에서 catch되어 다음 주기에 재시도된다
public class GeminiSummaryException extends RuntimeException {
    public GeminiSummaryException(String message, Throwable cause) {
        super(message, cause);
    }
}
