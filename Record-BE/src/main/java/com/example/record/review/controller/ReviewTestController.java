package com.example.record.review.controller;

import com.example.record.review.dto.request.ReviewCreateRequest;
import com.example.record.review.dto.request.ReviewUpdateRequest;
import com.example.record.review.dto.response.ReviewCreateResponse;
import com.example.record.review.dto.response.ReviewListItemResponse;
import com.example.record.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 리뷰 테스트 컨트롤러
 * 
 * 리뷰 CRUD 기능을 테스트하기 위한 간단한 엔드포인트들을 제공합니다.
 * 실제 운영 환경에서는 제거해야 합니다.
 * 
 * 왜 이 컨트롤러가 필요한가요?
 * 1. 기능 검증: 리뷰 생성, 조회, 수정, 삭제가 올바르게 작동하는지 확인
 * 2. 개발 편의성: 실제 프론트엔드 없이도 백엔드 기능을 테스트할 수 있음
 * 3. 디버깅: 문제가 발생했을 때 어느 부분에서 오류가 나는지 쉽게 파악 가능
 * 4. 학습 목적: 각 기능이 어떻게 작동하는지 단계별로 확인 가능
 */
@RestController
@RequestMapping("/api/test/reviews")
@RequiredArgsConstructor
public class ReviewTestController {

    private final ReviewService reviewService;

    /**
     * 리뷰 생성 테스트
     * 
     * 테스트용 데이터로 리뷰를 생성합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 리뷰 생성 기능 검증: 새로운 리뷰가 올바르게 저장되는지 확인
     * 2. 데이터 연동 테스트: 티켓, 질문 템플릿과의 관계가 제대로 설정되는지 확인
     * 3. 예외 처리 확인: 잘못된 데이터가 들어왔을 때 적절한 오류가 발생하는지 확인
     * 
     * @return 생성된 리뷰 정보
     */
    @PostMapping("/create-test")
    public ResponseEntity<ReviewCreateResponse> createTestReview() {
        // 테스트용 데이터 생성
        // 왜 이렇게 구성했나요?
        // 1. 실제 사용자가 입력할 수 있는 현실적인 데이터 사용
        // 2. 모든 필드를 포함해서 전체 기능을 테스트
        // 3. 여러 질문을 포함해서 복잡한 시나리오도 테스트
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .ticketId(1L)  // 실제 존재하는 티켓 ID로 변경 필요
                .summary("테스트 리뷰입니다. 이 공연은 정말 감동적이었습니다.")
                .keywords("감동, 뛰어난 연기, 아름다운 음악")
                .questions(List.of(
                        ReviewCreateRequest.QuestionDTO.builder()
                                .templateId(1L)  // 실제 존재하는 템플릿 ID로 변경 필요
                                .displayOrder(1)
                                .customText("이 공연에서 가장 인상깊었던 장면은 무엇인가요?")
                                .build(),
                        ReviewCreateRequest.QuestionDTO.builder()
                                .templateId(2L)  // 실제 존재하는 템플릿 ID로 변경 필요
                                .displayOrder(2)
                                .customText("배우들의 연기력은 어떠했나요?")
                                .build()
                ))
                .build();

        try {
            // 리뷰 생성 시도
            // try-catch를 사용하는 이유:
            // 1. 예상치 못한 오류가 발생해도 서버가 중단되지 않음
            // 2. 사용자에게 적절한 오류 메시지 전달 가능
            // 3. 테스트 중 오류 발생 시 원인 파악 용이
            ReviewCreateResponse response = reviewService.createReview(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 오류 발생 시 400 Bad Request 응답
            // 왜 400을 사용하나요?
            // - 클라이언트(테스트 요청)의 데이터에 문제가 있을 때 사용
            // - 서버 내부 오류(500)와 구분하여 문제 원인 파악 용이
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 리뷰 조회 테스트
     * 
     * 특정 사용자의 리뷰 목록을 조회합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 조회 기능 검증: 저장된 리뷰가 올바르게 조회되는지 확인
     * 2. 페이지네이션 테스트: 많은 리뷰가 있을 때 페이지별로 나누어 조회되는지 확인
     * 3. 권한 확인: 사용자별로 자신의 리뷰만 조회되는지 확인
     * 
     * @param userId 사용자 ID
     * @return 리뷰 목록
     */
    @GetMapping("/list/{userId}")
    public ResponseEntity<Page<ReviewListItemResponse>> getTestReviews(@PathVariable String userId) {
        try {
            // 사용자의 리뷰 목록 조회
            // PageRequest.of(0, 10)의 의미:
            // - 0: 첫 번째 페이지 (0부터 시작)
            // - 10: 한 페이지에 10개씩 표시
            // 이렇게 하는 이유: 실제 서비스에서도 페이지네이션을 사용하기 때문
            Page<ReviewListItemResponse> reviews = reviewService.getMyReviews(userId, PageRequest.of(0, 10));
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            // 조회 실패 시 400 Bad Request 응답
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 리뷰 수정 테스트
     * 
     * 기존 리뷰를 수정합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 수정 기능 검증: 기존 리뷰가 올바르게 수정되는지 확인
     * 2. 권한 확인: 본인의 리뷰만 수정할 수 있는지 확인
     * 3. 데이터 무결성: 수정 후에도 관련 데이터가 올바르게 유지되는지 확인
     * 
     * @param reviewId 수정할 리뷰 ID
     * @param userId 사용자 ID
     * @return 수정 완료 응답
     */
    @PatchMapping("/update/{reviewId}")
    public ResponseEntity<Void> updateTestReview(
            @PathVariable Long reviewId,
            @RequestParam String userId
    ) {
        // 테스트용 수정 데이터
        // 왜 이렇게 구성했나요?
        // 1. 기존 데이터와 다른 내용으로 수정하여 변경사항 확인 가능
        // 2. 요약, 키워드, 질문을 모두 포함하여 전체 수정 기능 테스트
        ReviewUpdateRequest request = ReviewUpdateRequest.builder()
                .summary("수정된 리뷰입니다. 이 공연은 정말 훌륭했습니다!")
                .keywords("훌륭한 공연, 완벽한 연기, 감동적인 스토리")
                .questions(List.of(
                        ReviewUpdateRequest.QuestionDTO.builder()
                                .templateId(1L)
                                .displayOrder(1)
                                .customText("수정된 질문: 이 공연의 하이라이트는 무엇인가요?")
                                .build()
                ))
                .build();

        try {
            // 리뷰 수정 시도
            reviewService.updateReview(reviewId, userId, request);
            // 수정 성공 시 200 OK 응답 (내용 없음)
            // 왜 200을 사용하나요?
            // - 수정이 성공적으로 완료되었음을 의미
            // - Void 타입이므로 응답 본문은 없음
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 수정 실패 시 400 Bad Request 응답
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 리뷰 삭제 테스트
     * 
     * 기존 리뷰를 삭제합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 삭제 기능 검증: 리뷰가 완전히 삭제되는지 확인
     * 2. 권한 확인: 본인의 리뷰만 삭제할 수 있는지 확인
     * 3. 연관 데이터 정리: 관련된 질문들도 함께 삭제되는지 확인
     * 
     * @param reviewId 삭제할 리뷰 ID
     * @param userId 사용자 ID
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/delete/{reviewId}")
    public ResponseEntity<Void> deleteTestReview(
            @PathVariable Long reviewId,
            @RequestParam String userId
    ) {
        try {
            // 리뷰 삭제 시도
            reviewService.deleteReview(reviewId, userId);
            // 삭제 성공 시 200 OK 응답 (내용 없음)
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            // 삭제 실패 시 400 Bad Request 응답
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 전체 테스트 실행
     * 
     * 리뷰 CRUD 기능을 순차적으로 테스트합니다.
     * 
     * 왜 이 메서드가 필요한가요?
     * 1. 통합 테스트: 모든 기능이 연계되어 작동하는지 확인
     * 2. 시나리오 테스트: 실제 사용자가 리뷰를 작성하는 전체 과정을 시뮬레이션
     * 3. 효율성: 한 번의 요청으로 모든 기능을 테스트할 수 있음
     * 4. 결과 확인: 각 단계별로 성공/실패 여부를 명확히 표시
     * 
     * @param userId 테스트할 사용자 ID
     * @return 테스트 결과
     */
    @PostMapping("/full-test/{userId}")
    public ResponseEntity<String> runFullTest(@PathVariable String userId) {
        // 테스트 결과를 저장할 StringBuilder 사용
        // 왜 StringBuilder를 사용하나요?
        // 1. 문자열을 여러 번 연결할 때 성능이 좋음
        // 2. 메모리 효율적 (String보다)
        // 3. 가독성 좋음 (단계별 결과 추가 가능)
        StringBuilder result = new StringBuilder();
        result.append("=== 리뷰 CRUD 테스트 시작 ===\n");

        try {
            // 1. 리뷰 생성 테스트
            result.append("1. 리뷰 생성 테스트...\n");
            ReviewCreateRequest createRequest = ReviewCreateRequest.builder()
                    .ticketId(1L)
                    .summary("전체 테스트용 리뷰입니다.")
                    .keywords("테스트, 검증, 기능 확인")
                    .questions(List.of(
                            ReviewCreateRequest.QuestionDTO.builder()
                                    .templateId(1L)
                                    .displayOrder(1)
                                    .customText("테스트 질문입니다.")
                                    .build()
                    ))
                    .build();

            ReviewCreateResponse createResponse = reviewService.createReview(createRequest);
            Long reviewId = createResponse.getReviewId();
            result.append("   ✅ 리뷰 생성 성공! ID: ").append(reviewId).append("\n");

            // 2. 리뷰 조회 테스트
            result.append("2. 리뷰 조회 테스트...\n");
            Page<ReviewListItemResponse> reviews = reviewService.getMyReviews(userId, PageRequest.of(0, 10));
            result.append("   ✅ 리뷰 조회 성공! 총 ").append(reviews.getTotalElements()).append("개 리뷰\n");

            // 3. 리뷰 수정 테스트
            result.append("3. 리뷰 수정 테스트...\n");
            ReviewUpdateRequest updateRequest = ReviewUpdateRequest.builder()
                    .summary("수정된 전체 테스트용 리뷰입니다.")
                    .keywords("수정됨, 테스트, 검증 완료")
                    .build();

            reviewService.updateReview(reviewId, userId, updateRequest);
            result.append("   ✅ 리뷰 수정 성공!\n");

            // 4. 리뷰 삭제 테스트
            result.append("4. 리뷰 삭제 테스트...\n");
            reviewService.deleteReview(reviewId, userId);
            result.append("   ✅ 리뷰 삭제 성공!\n");

            result.append("=== 모든 테스트 완료! ===\n");
            return ResponseEntity.ok(result.toString());

        } catch (Exception e) {
            // 테스트 중 오류 발생 시
            result.append("❌ 테스트 실패: ").append(e.getMessage()).append("\n");
            return ResponseEntity.badRequest().body(result.toString());
        }
    }
}
