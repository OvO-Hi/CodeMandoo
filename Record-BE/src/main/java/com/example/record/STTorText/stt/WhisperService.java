
package com.example.record.STTorText.stt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class WhisperService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.url.transcription:https://api.openai.com/v1/audio/transcriptions}")
    private String transcriptionUrl;

    @Value("${openai.model.transcription:whisper-1}")
    private String model;

    @Value("${openai.limits.whisperMaxFileMB:25}")
    private long maxFileMB;

    private final WebClient webClient = WebClient.builder()
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(c -> c.defaultCodecs().maxInMemorySize(8 * 1024 * 1024))
                    .build())
            .build();

    public String transcribe(byte[] audioBytes, String filename, String language) {
        long limitBytes = maxFileMB * 1024L * 1024L;
        if (audioBytes.length > limitBytes) {
            double sz = Math.round(audioBytes.length / 1024.0 / 1024.0 * 100) / 100.0;
            throw new IllegalArgumentException("파일이 너무 큽니다. (" + sz + "MB) 제한: " + maxFileMB + "MB");
        }

        ByteArrayResource filePart = new ByteArrayResource(audioBytes) {
            @Override public String getFilename() {
                return (filename == null || filename.isBlank()) ? "audio.m4a" : filename;
            }
        };

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", filePart);
        body.add("model", model);
        if (language != null && !language.isBlank()) body.add("language", language);

        // API 키가 비어있는지 확인 및 trim 처리 (디버깅용)
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API Key가 설정되지 않았습니다. 환경변수 OPENAI_API_KEY를 확인하세요.");
        }
        
        // API 키 앞뒤 공백 제거 (환경변수 설정 시 공백이 포함될 수 있음)
        apiKey = apiKey.trim();
        
        // 디버깅: API 키 일부만 로그 출력 (보안을 위해 앞 4자리와 뒤 4자리만)
        String maskedKey = apiKey.length() > 8 
                ? apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4)
                : "***";
        System.out.println("=== Whisper API 호출 시작 ===");
        System.out.println("API Key (마스킹): " + maskedKey);
        System.out.println("API Key 길이: " + apiKey.length());
        System.out.println("URL: " + transcriptionUrl);
        System.out.println("파일 크기: " + (audioBytes.length / 1024.0) + " KB");
        System.out.println("모델: " + model);
        System.out.println("언어: " + language);
        
        try {
            String result = webClient.post()
                    .uri(transcriptionUrl)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(WhisperResponse.class)
                    .timeout(Duration.ofSeconds(120))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500)))
                    .map(WhisperResponse::text)
                    .doOnSuccess(text -> {
                        System.out.println("=== Whisper API 호출 성공 ===");
                        System.out.println("변환된 텍스트 길이: " + (text != null ? text.length() : 0) + " 문자");
                        if (text != null && text.length() > 0) {
                            System.out.println("변환된 텍스트 미리보기: " + 
                                (text.length() > 100 ? text.substring(0, 100) + "..." : text));
                        }
                    })
                    .doOnError(error -> {
                        System.err.println("=== Whisper API 호출 오류 (doOnError) ===");
                        System.err.println("오류 타입: " + error.getClass().getName());
                        System.err.println("오류 메시지: " + error.getMessage());
                        if (error.getCause() != null) {
                            System.err.println("원인: " + error.getCause().getMessage());
                            System.err.println("원인 타입: " + error.getCause().getClass().getName());
                        }
                        error.printStackTrace();
                    })
                    .onErrorResume(e -> {
                        // 더 자세한 오류 정보를 포함하여 예외 발생
                        System.err.println("=== Whisper API 호출 오류 (onErrorResume) ===");
                        System.err.println("오류 타입: " + e.getClass().getName());
                        System.err.println("오류 메시지: " + e.getMessage());
                        if (e.getCause() != null) {
                            System.err.println("원인: " + e.getCause().getMessage());
                            System.err.println("원인 타입: " + e.getCause().getClass().getName());
                        }
                        e.printStackTrace();
                        return Mono.error(new RuntimeException("Whisper 요청 실패: " + e.getMessage(), e));
                    })
                    .block();
            
            System.out.println("=== Whisper API 호출 완료 ===");
            return result;
        } catch (Exception e) {
            System.err.println("=== Whisper API 호출 예외 발생 (catch 블록) ===");
            System.err.println("예외 타입: " + e.getClass().getName());
            System.err.println("예외 메시지: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("원인: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            throw new RuntimeException("Whisper 요청 실패: " + e.getMessage(), e);
        }
    }

    public record WhisperResponse(String text) {}
}
