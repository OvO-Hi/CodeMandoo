package com.example.record.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Component
@RequiredArgsConstructor
public class GptClient {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    @Value("${openai.timeout-ms:30000}")
    private long timeoutMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private WebClient openAi() {
        HttpClient http = HttpClient.create()
                .responseTimeout(java.time.Duration.ofMillis(timeoutMs));
        return WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .clientConnector(new ReactorClientHttpConnector(http))
                .defaultHeader("Content-Type", "application/json")
                .filter((request, next) -> {
                    ClientRequest mutated = ClientRequest.from(request)
                            .headers(h -> h.setBearerAuth(apiKey))
                            .build();
                    return next.exchange(mutated);
                })
                .build();
    }

    public String getStructuredJsonFromPrompt(String prompt) {
        String body = """
        {
          "model": %s,
          "messages": [{"role":"user","content": %s}],
          "temperature": 0.2,
          "response_format": {"type": "json_object"}
        }
        """.formatted(quote(model), quote(prompt));

        try {
            String raw = openAi().post()
                    .uri("/chat/completions")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(java.time.Duration.ofMillis(timeoutMs));

            JsonNode root = objectMapper.readTree(raw);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && choices.size() > 0) {
                JsonNode content = choices.get(0).path("message").path("content");
                if (content.isTextual()) return content.asText();
            }
            return raw;
        } catch (Exception e) {
            return "{\"error\":\"OpenAI call failed: " + safe(e.getMessage()) + "\"}";
        }
    }

    private static String quote(String s) {
        if (s == null) return "\"\"";
        String escaped = s.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n");
        return "\"" + escaped + "\"";
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\\","\\\\").replace("\"","\\\"").replace("\r","\\r").replace("\n","\\n");
    }
}
