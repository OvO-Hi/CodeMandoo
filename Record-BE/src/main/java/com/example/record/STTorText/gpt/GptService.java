package com.example.record.STTorText.gpt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GptService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model.chat:gpt-4o-mini}")
    private String model;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .exchangeStrategies(
                    ExchangeStrategies.builder()
                            .codecs(c -> c.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                            .build())
            .build();

    public String ask(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.4
        );

        Map<?, ?> response = webClient.post()
                .uri("/chat/completions")
                .headers(h -> h.setBearerAuth(apiKey))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .block();

        Map<?, ?> choice = (Map<?, ?>) ((List<?>) response.get("choices")).get(0);
        Map<?, ?> message = (Map<?, ?>) choice.get("message");
        return message.get("content").toString().trim();
    }
}
