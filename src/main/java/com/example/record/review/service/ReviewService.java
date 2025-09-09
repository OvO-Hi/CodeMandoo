// com.example.record.review.service

package com.example.record.review.service;

import com.example.record.review.dto.request.ReviewCreateRequest;
import com.example.record.review.dto.request.ReviewUpdateRequest;
import com.example.record.review.dto.response.ReviewCreateResponse;
import com.example.record.review.dto.response.ReviewListItemResponse;
import com.example.record.review.entity.Review;
import com.example.record.review.entity.ReviewQuestion;
import com.example.record.review.entity.Ticket;
import com.example.record.review.repository.ReviewRepository;
import com.example.record.review.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;

    /** 리뷰 생성 */
    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        // 1) ticket 확인
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("해당 티켓이 존재하지 않습니다: id=" + request.getTicketId()));

        // 2) Review 생성
        Review review = Review.builder()
                .ticket(ticket)
                .summary(request.getSummary())
                .keywords(request.getKeywords())
                .build();

        // 3) 질문 생성/연결
        if (request.getQuestions() != null) {
            for (var dto : request.getQuestions()) {
                ReviewQuestion q = ReviewQuestion.builder()
                        .templateId(dto.getTemplateId())
                        .displayOrder(dto.getDisplayOrder())
                        .customText(dto.getCustomText())
                        .build();
                review.addQuestion(q);
            }
        }

        // 4) 저장 (cascade로 questions도 저장)
        Review saved = reviewRepository.save(review);
        return ReviewCreateResponse.builder()
                .reviewId(saved.getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /** 내가 작성한 후기 목록 (userId = tickets.user_id) */
    public Page<ReviewListItemResponse> getMyReviews(String userId, Pageable pageable) {
        return reviewRepository.findByTicket_UserId(userId, pageable)
                .map(r -> ReviewListItemResponse.builder()
                        .reviewId(r.getId())
                        .ticketId(r.getTicket().getId())
                        .summary(r.getSummary())
                        .keywords(r.getKeywords())
                        .ticketImageUrl(r.getTicket().getImageUrl())
                        // PerformanceInfo의 title 필드가 있다면 아래 라인 유지, 없으면 null로 대체해도 됨
                        .performanceTitle(
                                r.getTicket().getPerformance() != null
                                        ? r.getTicket().getPerformance().getTitle()
                                        : null
                        )
                        .createdAt(r.getCreatedAt())
                        .build());
    }

    /** 후기 수정 (부분 업데이트) */
    @Transactional
    public void updateReview(Long reviewId, String requesterUserId, ReviewUpdateRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없습니다. id=" + reviewId));

        // 소유자 검증
        String owner = review.getTicket().getUserId();
        if (!owner.equals(requesterUserId)) {
            throw new SecurityException("본인 리뷰만 수정 가능합니다.");
        }

        // 부분 업데이트: null이면 변경 안 함
        if (req.getSummary() != null) review.setSummary(req.getSummary());
        if (req.getKeywords() != null) review.setKeywords(req.getKeywords());

        // 질문 변경 정책
        // - req.questions == null  → 질문 변경 안 함
        // - req.questions == []    → 모두 삭제
        // - req.questions 존재     → 전부 갈아끼우기
        if (req.getQuestions() != null) {
            review.getQuestions().clear(); // orphanRemoval=true 라서 DB에서도 삭제됨
            if (!req.getQuestions().isEmpty()) {
                if (review.getQuestions() == null) review.setQuestions(new ArrayList<>());
                for (var qdto : req.getQuestions()) {
                    ReviewQuestion q = ReviewQuestion.builder()
                            .templateId(qdto.getTemplateId())
                            .displayOrder(qdto.getDisplayOrder())
                            .customText(qdto.getCustomText())
                            .build();
                    review.addQuestion(q);
                }
            }
        }
        // 저장 호출 불필요: JPA dirty checking으로 자동 반영
    }

    /** 후기 삭제 */
    @Transactional
    public void deleteReview(Long reviewId, String requesterUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없습니다. id=" + reviewId));
        if (!review.getTicket().getUserId().equals(requesterUserId)) {
            throw new SecurityException("본인 리뷰만 삭제 가능합니다.");
        }
        reviewRepository.delete(review); // 자식 질문들은 orphanRemoval로 함께 삭제
    }
}
