// 이 클래스는 백엔드에서 프론트엔드로 "프롬프트 생성 결과"를 보낼 때 사용됩니다.
// 즉, 사용자가 입력한 공연 정보와 후기를 분석해 만든 프롬프트와 부가 정보를 담는 데이터 구조입니다.
/*
GPT 모델 응답을 담는 응답 DTO

OpenAIChatService가 GPT 응답을 받은 뒤 JSON 파싱 결과를 이 객체에 매핑

프론트엔드나 DB에 전달할 때 직렬화(Serialization)되어 사용됨.
 */
package com.example.record.promptcontrol_w03.dto;

import java.util.Map;

public class PromptResponse {

    // 실제로 GPT-4 이미지 생성에 쓰이는 텍스트 프롬프트입니다.
    // 예: "A dark musical theater scene about betrayal and revenge, set in 19th-century London..."
    private String prompt;

    // 추가적인 메타데이터입니다. 사용자가 직접 보진 않지만, 내부에서 유용하게 쓰일 수 있어요.
    // 예: 구조 유형(structure), 감정톤(tone), 추출된 키워드 리스트 등
    // {
    //     "structure": "뮤지컬",
    //     "style": "gothic",
    //     "tone": "emotional",
    //     "inferred_keywords": ["obsession", "conflict"]
    // }
    private Map<String, Object> meta;

    // ✅ Getter 메서드: 외부에서 prompt 값을 읽을 수 있게 해줍니다.
    public String getPrompt() {
        return prompt;
    }

    // ✅ Setter 메서드: 외부에서 prompt 값을 설정할 수 있게 해줍니다.
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    // ✅ Getter 메서드: meta 값을 읽을 수 있게 해줍니다.
    public Map<String, Object> getMeta() {
        return meta;
    }

    // ✅ Setter 메서드: meta 값을 설정할 수 있게 해줍니다.
    public void setMeta(Map<String, Object> meta) {
        this.meta = meta;
    }
}
