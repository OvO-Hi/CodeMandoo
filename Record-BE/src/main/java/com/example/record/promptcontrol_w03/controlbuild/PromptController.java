/*
역할: 프롬프트 생성 API 엔드포인트.

엔드포인트: POST /prompt

입력: PromptRequest (title, date, location, genre, cast, review, imageRequest 등)

로직: PromptService.generatePrompt() 호출 → 최종 프롬프트 생성

출력: PromptResponse (prompt 본문 + meta)
 */
package com.example.record.promptcontrol_w03.controlbuild;

import com.example.record.promptcontrol_w03.dto.PromptRequest;
import com.example.record.promptcontrol_w03.dto.PromptResponse;
import com.example.record.promptcontrol_w03.service.PromptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/prompt") // "/prompt" 경로로 들어오는 요청 처리
public class PromptController {

    private final PromptService promptService; // 프롬프트 생성 로직을 담당하는 서비스

    // 생성자 주입을 통한 의존성 주입
    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    // POST 요청을 받아 프롬프트를 생성한 후 응답으로 반환
    @PostMapping
    public ResponseEntity<PromptResponse> generatePrompt(@RequestBody PromptRequest request) {
        // 프롬프트 생성 수행
        PromptResponse response = promptService.generatePrompt(request);

        // HTTP 200 OK와 함께 프롬프트 응답 반환
        return ResponseEntity.ok(response);
    }
}
