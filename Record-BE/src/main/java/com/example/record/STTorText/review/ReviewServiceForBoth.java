package com.example.record.STTorText.review;

import com.example.record.promptcontrol_w03.service.OpenAIChatService;
import com.example.record.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ReviewServiceForBoth {

    private final OpenAIChatService openAI;

    /** =========================================
     * ① 후기 정리 (말투 유지 / 길이 유지 / 자연스럽게 정돈)
     * ========================================= */
    public String organize(ReviewRequest req, User user) {
        String input = req.text();
        if (!StringUtils.hasText(input)) {
            throw new IllegalArgumentException("review text is required");
        }

        String prompt = """
                아래 공연 후기를 '말투와 분위기를 최대한 유지'하면서
                자연스럽게 정돈된 한 문단으로 정리해줘.
                - 핵심만 정리하되 내용은 크게 축약하지 말 것
                - 말투, 감정선, 표현 분위기를 유지
                - 너무 딱딱하지 않고 사용자 후기 느낌을 살릴 것
                - 불필요한 반복/오타/비문만 자연스럽게 고치기
                - 불릿포인트 금지
                후기:
                %s
                """.formatted(input);

        return openAI.complete(
                "You rewrite Korean text naturally while keeping the user's tone.",
                prompt
        );
    }

    /** =========================================
     * ② 한국어 후기 요약 (3-5문장으로 핵심 정리)
     * ========================================= */
    public String summarize(ReviewRequest req, User user) {
        String base = req.text();
        if (!StringUtils.hasText(base)) {
            throw new IllegalArgumentException("review text is required");
        }

        String prompt = """
            아래 공연 후기를 **3-5문장의 자연스러운 한국어**로 요약해줘.
            요구사항:
            - 핵심 장면, 분위기, 감정, 공간적/분위기적 요소에 집중
            - 불릿포인트나 리스트 형식 금지
            - 요약에 대한 메타 코멘트 금지
            - 자연스럽고 읽기 좋은 문장으로 작성
            - 원본 후기의 감정과 분위기를 최대한 살리기
            
            후기:
            %s
            """.formatted(base);

        return openAI.complete(
                "You summarize Korean performance reviews into natural Korean (3-5 sentences) while preserving the original emotion and atmosphere.",
                prompt
        );
    }

}
