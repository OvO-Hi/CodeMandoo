package com.example.record.promptcontrol_w03.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class Gpt1PicService {

    @Value("${openai.url.image}")
    private String imageUrl;

    @Value("${openai.limits.imagePromptMaxChars:900}")
    private int maxChars;

    private final WebClient openAiWebClient;
    private final ObjectMapper objectMapper;

    /**
     * DALL-E 3 모델로 단일 이미지 생성(URL 반환)
     * 
     * 이유: OpenAI DALL-E API는 "gpt-image-1"이라는 모델이 존재하지 않습니다.
     *      DALL-E API는 "dall-e-2" 또는 "dall-e-3"를 사용하며,
     *      model 필드를 생략하면 기본값으로 "dall-e-3"가 사용됩니다.
     *      따라서 model 필드를 제거하고, size와 n 파라미터를 추가합니다.
     */
    public String generateSingleImageUrl(String prompt) {
        log.info("=== DALL-E 이미지 생성 시작 ===");
        log.info("이미지 URL 엔드포인트: {}", imageUrl);
        log.info("원본 프롬프트 길이: {} 문자", prompt.length());
        
        try {
            String safePrompt = prompt.length() <= maxChars
                    ? prompt
                    : prompt.substring(0, maxChars);
            
            log.info("안전한 프롬프트 길이: {} 문자", safePrompt.length());
            log.info("프롬프트 미리보기: {}", safePrompt.substring(0, Math.min(100, safePrompt.length())));

            Map<String, Object> body = new HashMap<>();
            // DALL-E API 요청 형식:
            // - prompt: 필수 (이미지 생성에 사용할 텍스트 설명)
            // - model: 선택사항 (dall-e-2 또는 dall-e-3, 생략 시 계정별 기본값)
            // - size: 선택사항 (1024x1024, 1792x1024, 1024x1792 중 하나)
            // - n: 선택사항 (생성할 이미지 개수, 1-10, 기본값 1)
            // 
            // 참고: 일부 OpenAI API 버전에서는 model 필드를 지원하지 않을 수 있으므로,
            //       prompt만 필수로 포함하고 나머지는 선택사항으로 설정합니다.
            body.put("prompt", safePrompt);
            body.put("model", "dall-e-3"); // 이유: 모델을 명시하지 않으면 계정 상태에 따라 400 오류가 발생할 수 있음
            body.put("size", "1024x1024");  // 이미지 크기 설정 (선택사항)
            body.put("n", 1);  // 생성할 이미지 개수 (선택사항, 기본값 1)

            log.info("DALL-E API 요청 body: {}", body);
            log.info("API 호출 시작...");

            String response = openAiWebClient.post()
                    .uri(imageUrl)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(60))
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)))
                    .block();
            
            log.info("=== DALL-E API 응답 받음 ===");
            log.info("응답 길이: {} 문자", response != null ? response.length() : 0);
            log.info("응답 미리보기: {}", response != null && response.length() > 0 
                    ? response.substring(0, Math.min(200, response.length())) : "null");

            JsonNode root = objectMapper.readTree(response);
            log.info("JSON 파싱 완료");
            
            JsonNode dataArray = root.path("data");
            log.info("data 배열 크기: {}", dataArray.isArray() ? dataArray.size() : 0);
            
            if (!dataArray.isArray() || dataArray.size() == 0) {
                log.error("응답에 data 배열이 없거나 비어있음");
                log.error("전체 응답: {}", response);
                throw new RuntimeException("Image generation failed: no data in response");
            }
            
            JsonNode first = dataArray.path(0);
            log.info("첫 번째 이미지 데이터 추출");

            // URL 우선
            String url = first.path("url").asText(null);
            if (url != null && !url.isBlank()) {
                log.info("=== 이미지 URL 추출 성공 ===");
                log.info("이미지 URL: {}", url);
                return url;
            }

            // b64 처리 (S3 업로드 필요)
            String b64 = first.path("b64_json").asText(null);
            if (b64 != null && !b64.isBlank()) {
                log.warn("b64_json 응답 받음 (S3 업로드 미구현)");
                throw new UnsupportedOperationException(
                        "b64_json returned — implement S3 upload and return public URL."
                );
            }

            log.error("응답에 url 또는 b64_json이 없음");
            log.error("첫 번째 데이터 노드: {}", first.toString());
            throw new RuntimeException("Image generation failed: missing url/b64_json");

        } catch (Exception e) {
            if (e instanceof WebClientResponseException wex) {
                // 이유: OpenAI에서 전달한 정확한 실패 원인(JSON)을 기록하여 추후 프롬프트 튜닝/에러 분석에 활용합니다.
                log.error("OpenAI 응답 본문: {}", wex.getResponseBodyAsString());
            }
            log.error("=== DALL-E 이미지 생성 실패 ===");
            log.error("오류 타입: {}", e.getClass().getSimpleName());
            log.error("오류 메시지: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("원인: {}", e.getCause().getMessage());
            }
            throw new RuntimeException("DALL-E image generation failed: " + e.getMessage(), e);
        }
    }

    public String generateImageUrlOnly(String prompt) {
        return generateSingleImageUrl(prompt);
    }
}
