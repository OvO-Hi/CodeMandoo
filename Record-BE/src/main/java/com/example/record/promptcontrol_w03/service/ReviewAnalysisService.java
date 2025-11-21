package com.example.record.promptcontrol_w03.service;
/*
역할: 후기 텍스트 → 구조화 JSON 추출.

핵심 기능

OpenAIChatService.complete() 호출 시 시스템 프롬프트를 “compact JSON만” 요구

응답 문자열에서 JSON 부분만 추출(extractJson) 후 ObjectMapper로 Map 변환

파싱 실패 시 {"error":"JSON parse failed","raw":...} 형태로 안전 반환
 */
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewAnalysisService {

    private final OpenAIChatService openAI;
    private final ObjectMapper mapper;

    /** 모델이 코드펜스/설명을 섞어도 "순수 JSON"만 추출 */
    private static String extractJson(String content) {
        if (content == null || content.isEmpty()) return "{}";
        if (content.startsWith("```")) {
            int start = content.indexOf('{');
            int end = content.lastIndexOf('}');
            if (start >= 0 && end > start) return content.substring(start, end + 1);
        }
        int s = content.indexOf('{');
        int e = content.lastIndexOf('}');
        return (s >= 0 && e > s) ? content.substring(s, e + 1) : content;
    }

    /** 공연 후기 → 영문 분석 JSON */
    public Map<String, Object> analyzeReview(String reviewText) {
        String userPrompt = """
            Analyze the following performance review and return ONLY JSON (no explanations, no code blocks).
            Keys: emotion, theme, setting, relationship, actions, character1, character2, (character3, character4 if available), lighting
            IMPORTANT: Return all values in ENGLISH only. Translate Korean words/phrases to English.
            Review: %s
        """.formatted(reviewText);

        String response = openAI.complete(
                "You analyze performance reviews and reply strictly as compact JSON.",
                userPrompt
        );

        try {
            String jsonOnly = extractJson(response);
            return mapper.readValue(jsonOnly, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("error", "JSON parse failed", "raw", response);
        }
    }
}
