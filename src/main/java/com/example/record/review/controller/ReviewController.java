// 경로: com.example.record.review.controller.ReviewController

package com.example.record.review.controller;

import com.example.record.review.dto.request.ReviewCreateRequest;
import com.example.record.review.dto.request.ReviewUpdateRequest;
import com.example.record.review.dto.response.ReviewCreateResponse;
import com.example.record.review.dto.response.ReviewListItemResponse;
import com.example.record.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 후기 생성 */
    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(@RequestBody ReviewCreateRequest request) {
        ReviewCreateResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 내가 작성한 후기 목록 조회 */
    @GetMapping("/me/{userId}")
    public ResponseEntity<Page<ReviewListItemResponse>> getMyReviews(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReviewListItemResponse> res = reviewService.getMyReviews(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(res);
    }

    /** 후기 수정 (부분 업데이트) */
    @PatchMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestParam String userId, // 실제 운영에선 JWT에서 추출 권장
            @RequestBody ReviewUpdateRequest request
    ) {
        reviewService.updateReview(reviewId, userId, request);
        return ResponseEntity.noContent().build();
    }

    /** 후기 삭제 */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestParam String userId // 실제 운영에선 JWT에서 추출 권장
    ) {
        reviewService.deleteReview(reviewId, userId);
        return ResponseEntity.noContent().build();
    }
}
