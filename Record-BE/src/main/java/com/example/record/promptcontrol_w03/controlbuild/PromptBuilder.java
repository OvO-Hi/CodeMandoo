package com.example.record.promptcontrol_w03.controlbuild;
/*
역할: 최종 “이미지 프롬프트” 조립(간단 버전).

핵심 규칙

shortReview(필수, 2~3문장) + imageRequest(선택) 결합

전체 문장수 4~5로 제한(부족하면 보강 문장 추가, 초과하면 5문장으로 컷)

마지막에 텍스트 금지 규칙 추가:
 */
public class PromptBuilder {

    private static final String NO_TEXT_RULE =
            "No captions, no letters, no words, no logos, no watermarks.";

    private PromptBuilder() {}

    /**
     * 최종 이미지 프롬프트를 조립합니다.
     * - shortReview: 2~3문장 시각 묘사 (필수)
     * - imageRequest: "푸른 색 기반", "귀여운 그림체", "따뜻한 일러스트" 등 추가 요청(선택)
     * - 항상 텍스트 금지 규칙을 덧붙입니다.
     * - 전체 문장 수는 4~5문장 사이로 제한합니다.
     */
    public static String buildImagePrompt(String shortReview, String imageRequest) {
        StringBuilder sb = new StringBuilder();

        if (shortReview != null && !shortReview.isBlank()) {
            sb.append(shortReview.trim());
        }

        if (imageRequest != null && !imageRequest.isBlank()) {
            sb.append(" ");
            sb.append("추가 요청: ").append(imageRequest.trim());
        }

        // 문장 수 제한 (4~5문장)
        String combined = sb.toString().trim();
        int sentenceCount = countSentences(combined);

        if (sentenceCount < 4) {
            sb.append(" ").append("프롬프트를 풍부하게 만들어 시각적 디테일을 강조합니다.");
        } else if (sentenceCount > 5) {
            combined = shortenToFiveSentences(combined);
            sb = new StringBuilder(combined);
        }

        sb.append("\n").append(NO_TEXT_RULE);
        return sb.toString().trim();
    }

    /** 마침표 기준으로 문장 수 세기 */
    private static int countSentences(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.split("[.!?？。]+").length;
    }

    /** 문장 5개까지만 남기고 잘라내기 */
    private static String shortenToFiveSentences(String text) {
        if (text == null) return "";
        String[] sentences = text.split("(?<=[.!?？。])\\s*");
        StringBuilder result = new StringBuilder();
        int limit = Math.min(sentences.length, 5);
        for (int i = 0; i < limit; i++) {
            result.append(sentences[i].trim());
            if (i < limit - 1) result.append(" ");
        }
        return result.toString();
    }
}
