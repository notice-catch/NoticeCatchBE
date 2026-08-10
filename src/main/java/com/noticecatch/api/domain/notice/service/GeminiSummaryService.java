package com.noticecatch.api.domain.notice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noticecatch.api.domain.notice.dto.response.NoticeDetailResponse.AiSummaryDto;
import com.noticecatch.api.domain.notice.exception.GeminiSummaryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiSummaryService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-lite-latest:generateContent}")
    private String geminiUrl;

    private final WebClient webClient = WebClient.builder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiSummaryDto generateSummary(String noticeTitle, String noticeContent) {
        try {
            String prompt = String.format("""
                너는 대학생을 위한 공지사항 요약 도우미야.
                아래 공지사항 제목과 본문을 읽고 '자격조건', '혜택내용', '마감일시'를 각각 한 문장(30자 이내)으로 요약해줘.
                
                [공지 제목]: %s
                [공지 본문]: %s

                응답은 반드시 아래 JSON 키 값을 사용해줘:
                {
                  "eligibility": "요약된 지원 자격 내용",
                  "benefit": "요약된 혜택 내용",
                  "deadline": "요약된 마감일시 정보"
                }
                """, noticeTitle, noticeContent);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    ),
                    "generationConfig", Map.of(
                            "responseMimeType", "application/json"
                    )
            );

            String responseString = webClient.post()
                    .uri(geminiUrl + "?key=" + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode rootNode = objectMapper.readTree(responseString);
            String textResponse = rootNode.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText().trim();

            JsonNode summaryJson = objectMapper.readTree(textResponse);

            return AiSummaryDto.builder()
                    .eligibility(summaryJson.path("eligibility").asText("해당 사항 없음"))
                    .benefit(summaryJson.path("benefit").asText("해당 사항 없음"))
                    .deadline(summaryJson.path("deadline").asText("상세 공지 참조"))
                    .build();

        } catch (Exception e) {
            log.error("Gemini AI 요약 생성 중 오류 발생: {}", e.getMessage());
            // 실패를 더미값으로 감추면 배치가 "성공"으로 착각해 저장해버리므로, 예외를 던져 재시도 대상으로 남긴다
            throw new GeminiSummaryException("Gemini AI 요약 생성에 실패했습니다.", e);
        }
    }
}