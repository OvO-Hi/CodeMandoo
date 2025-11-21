package com.example.record.STTorText.review;

import com.example.record.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/review")   // reviews → review
public class ReviewController {

    private final ReviewServiceForBoth reviewService;

    /**
     * 후기 정리 (말투 유지 / 길이 유지 / 자연스럽게 정돈)
     * 
     * 이유: 프론트엔드의 apiClient가 기대하는 success/data/message 구조로 응답해야
     *      성공/실패 여부를 일관되게 처리할 수 있습니다.
     */
    @PostMapping("/organize")
    public ResponseEntity<?> organize(
            @RequestBody ReviewRequest req
    ) {
        try {
            String organizedText = reviewService.organize(req, null);
            return ResponseEntity.ok(new ApiResponse<>(true, organizedText, "후기 정리가 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(422)
                    .body(new ApiResponse<>(false, null, "후기 정리 실패: " + e.getMessage()));
        }
    }

    /**
     * 후기 5줄 요약 (영어로 변환하여 이미지 프롬프트용)
     * 
     * 이유: 프론트엔드의 apiClient가 기대하는 success/data/message 구조로 응답해야
     *      성공/실패 여부를 일관되게 처리할 수 있습니다.
     */
    @PostMapping("/summarize")
    public ResponseEntity<?> summarize(
            @RequestBody ReviewRequest req
    ) {
        try {
            String summarizedText = reviewService.summarize(req, null);
            return ResponseEntity.ok(new ApiResponse<>(true, summarizedText, "후기 요약이 완료되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(422)
                    .body(new ApiResponse<>(false, null, "후기 요약 실패: " + e.getMessage()));
        }
    }
}
