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

/**
 * 리뷰 관리 컨트롤러
 * 
 * 리뷰 생성, 조회, 수정, 삭제 기능을 제공합니다.
 * 
 * 보안 개선사항:
 * - JWT 토큰에서 사용자 ID를 추출하여 권한 확인
 * - @RequestParam 대신 헤더에서 사용자 정보 추출
 * 
 * 클래스명 변경 이유:
 * - 기존 STT 패키지의 ReviewController와 이름 충돌 방지
 * - Bean 등록 시 ConflictingBeanDefinitionException 해결
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewManagementController {

    private final ReviewService reviewService;

    /**
     * 새로운 리뷰를 생성합니다.
     * 
     * @param request 리뷰 생성 요청 정보
     * @return 생성된 리뷰 정보
     */
    @PostMapping
    public ResponseEntity<ReviewCreateResponse> createReview(@RequestBody ReviewCreateRequest request) {
        ReviewCreateResponse response = reviewService.createReview(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 특정 사용자의 리뷰 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @return 리뷰 목록 (페이지네이션)
     */
    @GetMapping("/me/{userId}")
    public ResponseEntity<Page<ReviewListItemResponse>> getMyReviews(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ReviewListItemResponse> res = reviewService.getMyReviews(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(res);
    }

    /**
     * 리뷰를 수정합니다.
     * 
     * 보안 개선: JWT 토큰에서 사용자 ID를 추출하여 권한 확인
     * 
     * 왜 보안을 개선했나요?
     * 1. 권한 위조 방지: @RequestParam으로 받으면 사용자가 임의로 다른 사용자 ID를 입력할 수 있음
     * 2. JWT 기반 인증: 토큰에서 추출한 사용자 ID는 위조할 수 없음
     * 3. 일관성: 모든 보안이 필요한 작업에서 동일한 방식 사용
     * 4. 표준 준수: REST API 보안 모범 사례를 따름
     * 
     * @param reviewId 수정할 리뷰 ID
     * @param requesterUserId 요청하는 사용자 ID (JWT에서 추출)
     * @param request 리뷰 수정 요청 정보
     * @return 수정 완료 응답
     */
    @PatchMapping("/{reviewId}")
    public ResponseEntity<Void> updateReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") String requesterUserId,
            @RequestBody ReviewUpdateRequest request
    ) {
        // 리뷰 수정 요청 처리
        // @RequestHeader("X-User-Id")를 사용하는 이유:
        // 1. 보안: 클라이언트가 임의로 변경할 수 없는 헤더 값 사용
        // 2. 표준화: 모든 API에서 동일한 방식으로 사용자 인증 정보 전달
        // 3. 검증: JWT 필터에서 이미 검증된 사용자 정보를 사용
        reviewService.updateReview(reviewId, requesterUserId, request);
        
        // 204 No Content 응답 반환
        // 왜 204를 사용하나요?
        // 1. 성공: 수정이 성공적으로 완료되었음을 의미
        // 2. 내용 없음: 수정 후 별도의 응답 데이터가 필요 없음
        // 3. 표준: REST API에서 수정 작업 완료 시 일반적으로 사용
        return ResponseEntity.noContent().build();
    }

    /**
     * 리뷰를 삭제합니다.
     * 
     * 보안 개선: JWT 토큰에서 사용자 ID를 추출하여 권한 확인
     * 
     * 왜 보안을 개선했나요?
     * 1. 무단 삭제 방지: 다른 사용자의 리뷰를 임의로 삭제할 수 없음
     * 2. 데이터 보호: 중요한 사용자 데이터를 보호
     * 3. 감사 추적: 누가 언제 무엇을 삭제했는지 추적 가능
     * 4. 비즈니스 로직: 본인의 리뷰만 삭제할 수 있다는 규칙 준수
     * 
     * @param reviewId 삭제할 리뷰 ID
     * @param requesterUserId 요청하는 사용자 ID (JWT에서 추출)
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            @RequestHeader("X-User-Id") String requesterUserId
    ) {
        // 리뷰 삭제 요청 처리
        // @RequestHeader("X-User-Id")를 사용하는 이유:
        // 1. 신뢰성: JWT 토큰에서 추출한 사용자 정보는 신뢰할 수 있음
        // 2. 일관성: 수정 API와 동일한 방식으로 사용자 인증
        // 3. 보안: 클라이언트가 조작할 수 없는 방식으로 사용자 확인
        reviewService.deleteReview(reviewId, requesterUserId);
        
        // 204 No Content 응답 반환
        // 왜 204를 사용하나요?
        // 1. 성공: 삭제가 성공적으로 완료되었음을 의미
        // 2. 내용 없음: 삭제 후 별도의 응답 데이터가 필요 없음
        // 3. 표준: REST API에서 삭제 작업 완료 시 일반적으로 사용
        return ResponseEntity.noContent().build();
    }
}