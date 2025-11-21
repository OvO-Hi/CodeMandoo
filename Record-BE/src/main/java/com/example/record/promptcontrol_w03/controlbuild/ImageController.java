package com.example.record.promptcontrol_w03.controlbuild;

import com.example.record.common.ApiResponse;
import com.example.record.promptcontrol_w03.dto.ImageResponse;
import com.example.record.promptcontrol_w03.dto.PromptRequest;
import com.example.record.promptcontrol_w03.dto.PromptResponse;
import com.example.record.promptcontrol_w03.service.Gpt1PicService;
import com.example.record.promptcontrol_w03.service.PromptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/generate-image")
public class ImageController {

    private final Gpt1PicService gpt1PicService;
    private final PromptService promptService; // 영어 프롬프트 생성을 위한 서비스

    /** ★ JSON 기반 이미지 생성 */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> generateJson(@RequestBody PromptRequest request) {
        log.info("=== 이미지 생성 요청 시작 ===");
        log.info("title: {}", request.getTitle());
        log.info("genre: {}", request.getGenre());
        log.info("basePrompt: {}", request.getBasePrompt());
        log.info("imageRequest: {}", request.getImageRequest());
        try {
            ImageResponse response = generateInternal(request);
            log.info("=== 이미지 생성 성공 ===");
            log.info("생성된 이미지 URL: {}", response.getImageUrl());
            // 이유: 프론트엔드 apiClient가 기대하는 success/data/message 구조로 응답
            return ResponseEntity.ok(new ApiResponse<>(true, response, "이미지 생성이 완료되었습니다."));
        } catch (Exception e) {
            log.error("=== 이미지 생성 실패 ===");
            log.error("오류: {}", e.getMessage(), e);
            // 이유: 실패 응답도 동일한 포맷을 사용해야 프론트에서 메시지를 파싱해 사용자에게 안내할 수 있다.
            return ResponseEntity.ok(new ApiResponse<>(false, null, "이미지 생성 실패: " + e.getMessage()));
        }
    }

    /** ★ JSON + 파일 기반 이미지 생성 */
    @PostMapping(value = "/with-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ImageResponse>> generateWithFile(
            @RequestPart("request") PromptRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) {
        log.info("=== 이미지 생성 요청 시작 (파일 포함) ===");
        log.info("title: {}", request.getTitle());
        log.info("genre: {}", request.getGenre());
        log.info("basePrompt: {}", request.getBasePrompt());
        log.info("imageRequest: {}", request.getImageRequest());
        try {
            ImageResponse response = generateInternal(request);
            log.info("=== 이미지 생성 성공 ===");
            log.info("생성된 이미지 URL: {}", response.getImageUrl());
            // 이유: 프론트엔드 apiClient가 기대하는 success/data/message 구조로 응답
            return ResponseEntity.ok(new ApiResponse<>(true, response, "이미지 생성이 완료되었습니다."));
        } catch (Exception e) {
            log.error("=== 이미지 생성 실패 ===");
            log.error("오류: {}", e.getMessage(), e);
            // 이유: 실패 응답도 동일한 포맷을 사용해야 프론트에서 메시지를 파싱해 사용자에게 안내할 수 있다.
            return ResponseEntity.ok(new ApiResponse<>(false, null, "이미지 생성 실패: " + e.getMessage()));
        }
    }

    /** ★ 최종 이미지 생성 공통 처리 */
    private ImageResponse generateInternal(PromptRequest request) {
        log.info("generateInternal 호출됨 - title: {}, genre: {}, basePrompt: {}, imageRequest: {}", 
                request.getTitle(), request.getGenre(), request.getBasePrompt(), request.getImageRequest());

        if (request.getBasePrompt() == null || request.getBasePrompt().isBlank()) {
            log.warn("basePrompt가 비어있음");
            return ImageResponse.error("basePrompt (Korean review summary) is required");
        }

        // PromptService를 사용하여 영어 프롬프트 생성
        // 이유: DALL-E는 영어 프롬프트를 기대하므로, 한국어 basePrompt를 영어로 변환하고
        //      DB의 musical_db, musical_characters 정보를 활용하여 더 풍부한 프롬프트를 생성합니다.
        try {
            log.info("PromptService를 사용하여 영어 프롬프트 생성 시작...");
            PromptResponse promptResponse = promptService.generatePrompt(request);
            String finalPrompt = promptResponse.getPrompt();
            
            log.info("=== 영어 프롬프트 생성 완료 ===");
            log.info("생성된 영어 프롬프트: {}", finalPrompt);
            log.info("프롬프트 길이: {} 문자", finalPrompt.length());

            // DALL-E API 호출
            String imageUrl = gpt1PicService.generateSingleImageUrl(finalPrompt);
            log.info("이미지 URL 생성 완료: {}", imageUrl);

            ImageResponse res = new ImageResponse();
            res.setPrompt(finalPrompt);
            res.setImageUrl(imageUrl);

            return res;
        } catch (Exception e) {
            log.error("이미지 생성 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}
