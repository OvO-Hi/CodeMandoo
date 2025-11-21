package com.example.record.promptcontrol_w03.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpenAIChatService {

    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.url.chat}")
    private String chatUrl;

    @Value("${openai.model.chat}")
    private String model;

    /**
     * OpenAI Chat API 호출
     * 
     * 이유: 요약 기능은 3-5 문장의 영어 텍스트를 생성해야 하므로,
     *      max_tokens를 500으로 늘려 충분한 응답을 받을 수 있도록 합니다.
     *      또한 타임아웃을 60초로 늘려 긴 텍스트 처리 시에도 안정적으로 동작하도록 합니다.
     */
    public String complete(String systemPrompt, String userPrompt) {
        ChatRequest req = new ChatRequest();
        req.model = model;
        req.temperature = 0.7;
        req.max_tokens = 500;   // 요약은 3-5 문장이므로 500 토큰으로 충분히 여유있게 설정
        req.messages = List.of(
                Message.text("system", systemPrompt),
                Message.text("user", userPrompt)
        );

        try {
            System.out.println("=== OpenAI Chat API 호출 시작 ===");
            System.out.println("모델: " + model);
            System.out.println("시스템 프롬프트: " + systemPrompt);
            System.out.println("사용자 프롬프트 길이: " + userPrompt.length() + " 문자");
            
            // 응답을 String으로 받아서 수동으로 파싱
            // 이유: OpenAI API 응답의 content 필드가 문자열 또는 배열로 올 수 있어서,
            //      WebClient의 자동 deserialization이 실패할 수 있습니다.
            //      따라서 JSON 문자열을 직접 받아서 ObjectMapper로 유연하게 파싱합니다.
            String responseJson = openAiWebClient.post()
                    .uri(chatUrl)
                    .bodyValue(req)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> {
                        System.err.println("=== OpenAI Chat API HTTP 오류 ===");
                        System.err.println("상태 코드: " + r.statusCode());
                        return r.bodyToMono(String.class)
                                .doOnNext(body -> System.err.println("오류 응답 본문: " + body))
                                .map(body -> new RuntimeException("OpenAI chat error: HTTP " + r.statusCode() + " - " + body));
                    })
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))  // 요약은 시간이 더 걸릴 수 있으므로 60초로 증가
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(2)))  // 재시도 간격을 2초로 증가
                    .doOnSuccess(json -> {
                        System.out.println("=== OpenAI Chat API 호출 성공 ===");
                        try {
                            JsonNode root = objectMapper.readTree(json);
                            JsonNode choices = root.get("choices");
                            if (choices != null && choices.isArray() && choices.size() > 0) {
                                JsonNode message = choices.get(0).get("message");
                                if (message != null) {
                                    String content = extractContent(message.get("content"));
                                    System.out.println("응답 길이: " + content.length() + " 문자");
                                    System.out.println("응답 미리보기: " + 
                                        (content.length() > 100 ? content.substring(0, 100) + "..." : content));
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("응답 파싱 중 오류 (로깅용): " + e.getMessage());
                        }
                    })
                    .doOnError(error -> {
                        System.err.println("=== OpenAI Chat API 호출 오류 ===");
                        System.err.println("오류 타입: " + error.getClass().getName());
                        System.err.println("오류 메시지: " + error.getMessage());
                        if (error.getCause() != null) {
                            System.err.println("원인: " + error.getCause().getMessage());
                        }
                        error.printStackTrace();
                    })
                    .block();

            if (responseJson == null || responseJson.isEmpty()) {
                throw new RuntimeException("Empty OpenAI response");
            }

            // JSON 파싱하여 content 추출
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                throw new RuntimeException("Empty choices in OpenAI response");
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new RuntimeException("Empty message in OpenAI response");
            }

            String result = extractContent(message.get("content")).trim();
            System.out.println("=== OpenAI Chat API 호출 완료 ===");
            return result;

        } catch (Exception e) {
            System.err.println("=== OpenAI Chat API 예외 발생 ===");
            System.err.println("예외 타입: " + e.getClass().getName());
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("OpenAI chat call failed: " + e.getMessage(), e);
        }
    }

    // ==== 내부 DTO ====

    @Data
    static class ChatRequest {
        public String model;
        public List<Message> messages;
        public Double temperature;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Integer max_tokens;
    }

    @Data
    static class Message {
        public String role;
        public List<Content> content;

        public static Message text(String role, String text) {
            Message m = new Message();
            m.role = role;
            m.content = List.of(new Content("text", text));
            return m;
        }

        @Data
        static class Content {
            public String type;
            public String text;

            public Content(String type, String text) {
                this.type = type;
                this.text = text;
            }
        }
    }

    /**
     * JSON의 content 필드에서 텍스트를 추출합니다.
     * 
     * 이유: OpenAI API 응답에서 content는 문자열 또는 배열로 올 수 있습니다.
     *      - 문자열인 경우: 그대로 반환
     *      - 배열인 경우: 첫 번째 요소의 text 필드를 반환
     * 
     * @param contentNode JSON의 content 노드
     * @return 추출된 텍스트 문자열
     */
    private String extractContent(JsonNode contentNode) {
        if (contentNode == null) {
            return "";
        }
        
        // content가 문자열인 경우
        if (contentNode.isTextual()) {
            return contentNode.asText();
        }
        
        // content가 배열인 경우 (하위 호환성)
        if (contentNode.isArray() && contentNode.size() > 0) {
            JsonNode firstItem = contentNode.get(0);
            if (firstItem != null && firstItem.has("text")) {
                return firstItem.get("text").asText();
            }
        }
        
        // 예상치 못한 형식인 경우
        return contentNode.asText();
    }
}
