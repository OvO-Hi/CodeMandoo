package com.example.record.review.service;

import com.example.record.review.dto.request.ReviewCreateRequest;
import com.example.record.review.dto.request.ReviewUpdateRequest;
import com.example.record.review.dto.response.ReviewCreateResponse;
import com.example.record.review.dto.response.ReviewListItemResponse;
import com.example.record.review.entity.QuestionTemplate;
import com.example.record.review.entity.Review;
import com.example.record.review.entity.ReviewQuestion;
import com.example.record.review.entity.Ticket;
import com.example.record.review.repository.QuestionTemplateRepository;
import com.example.record.review.repository.ReviewRepository;
import com.example.record.review.repository.TicketRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketRepository ticketRepository;
    private final QuestionTemplateRepository questionTemplateRepository;
    private final ReviewQuestionService reviewQuestionService;

    @Transactional
    public ReviewCreateResponse createReview(ReviewCreateRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new IllegalArgumentException("해당 티켓이 존재하지 않습니다: id=" + request.getTicketId()));

        Review review = Review.builder()
                .ticket(ticket)
                .summary(request.getSummary())
                .keywords(request.getKeywords())
                .build();

        if (request.getQuestions() != null) {
            for (var dto : request.getQuestions()) {
                /**
                 * 질문 템플릿 조회 및 설정
                 * 
                 * 변경 사항:
                 * - templateId(Long) → template(QuestionTemplate 객체)로 변경
                 * - 이유: ReviewQuestion 엔티티에서 templateId 필드를 QuestionTemplate 객체로 변경했기 때문
                 * 
                 * 이렇게 하면:
                 * 1. 데이터 무결성 보장: 존재하지 않는 템플릿 ID로 질문을 만들 수 없음
                 * 2. 조인 쿼리 가능: 템플릿 정보와 함께 질문을 조회할 수 있음
                 * 3. 객체지향 설계: question.getTemplate().getTemplateText()로 자연스럽게 접근 가능
                 */
                QuestionTemplate template = questionTemplateRepository.findById(dto.getTemplateId())
                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문 템플릿입니다: id=" + dto.getTemplateId()));
                
                ReviewQuestion q = ReviewQuestion.builder()
                        .template(template)  // templateId → template으로 변경
                        .displayOrder(dto.getDisplayOrder())
                        .customText(dto.getCustomText())
                        .build();
                review.addQuestion(q);
            }
        }

        Review saved = reviewRepository.save(review);
        
        // 후기 생성 후, 사용자의 후기 개수를 확인하여 맞춤 질문 생성 여부 결정
        // 3개, 6개, 9개... 이런 식으로 3개씩 늘어날 때마다 분석
        String userId = ticket.getUser().getId();
        long reviewCount = reviewRepository.findByTicket_User_IdOrderByCreatedAtAsc(userId).size();
        
        // 3개, 6개, 9개... 이런 식으로 3개씩 늘어날 때마다 분석
        if (reviewCount % 3 == 0 && reviewCount >= 3) {
            log.info("사용자 {}의 후기 개수가 {}개가 되어 맞춤 질문 생성 시작", userId, reviewCount);
            try {
                // 비동기로 실행하여 응답 지연 방지 (선택적)
                reviewQuestionService.analyzeAndGenerateCustomQuestions(userId);
            } catch (Exception e) {
                log.error("맞춤 질문 생성 중 오류 발생: {}", e.getMessage(), e);
                // 오류가 발생해도 후기 생성은 성공한 것으로 처리
            }
        }
        
        return ReviewCreateResponse.builder()
                .reviewId(saved.getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    /**
     * 특정 사용자의 리뷰 목록을 페이지네이션과 함께 조회합니다.
     * 
     * 변경 사항 설명:
     * - findByTicket_UserId → findByTicket_User_Id로 변경
     * - 이유: Ticket 엔티티에서 userId(String) → user(User 객체)로 변경했기 때문
     * - JPA는 객체 관계를 통해 쿼리를 생성하므로, user.id로 접근해야 합니다.
     */
    public Page<ReviewListItemResponse> getMyReviews(String userId, Pageable pageable) {
        return reviewRepository.findByTicket_User_Id(userId, pageable)
                .map(r -> ReviewListItemResponse.builder()
                        .reviewId(r.getId())
                        .ticketId(r.getTicket().getId())
                        .summary(r.getSummary())
                        .keywords(r.getKeywords())
                        .ticketImageUrl(r.getTicket().getImageUrl())
                        .performanceTitle(r.getTicket().getPerformanceTitle())
                        .createdAt(r.getCreatedAt())
                        .build());
    }

    @Transactional
    public void updateReview(Long reviewId, String requesterUserId, ReviewUpdateRequest req) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없습니다. id=" + reviewId));

        /**
         * 리뷰 소유자 확인 로직
         * 
         * 변경 사항: review.getTicket().getUserId() → review.getTicket().getUser().getId()
         * 이유: Ticket 엔티티에서 userId 필드를 User 객체로 변경했기 때문
         * 이제 사용자 ID에 접근하려면 user.getId()를 사용해야 합니다.
         */
        String owner = review.getTicket().getUser().getId();
        if (!owner.equals(requesterUserId)) {
            throw new SecurityException("본인 리뷰만 수정 가능합니다.");
        }

        if (req.getSummary() != null) review.setSummary(req.getSummary());
        if (req.getKeywords() != null) review.setKeywords(req.getKeywords());

        if (req.getQuestions() != null) {
            review.getQuestions().clear();
            if (!req.getQuestions().isEmpty()) {
                for (var qdto : req.getQuestions()) {
                    /**
                     * 질문 템플릿 조회 및 설정 (리뷰 수정 시)
                     * 
                     * createReview와 동일한 로직을 적용합니다.
                     * templateId → template 객체로 변경하여 데이터 무결성을 보장합니다.
                     */
                    QuestionTemplate template = questionTemplateRepository.findById(qdto.getTemplateId())
                            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 질문 템플릿입니다: id=" + qdto.getTemplateId()));
                    
                    ReviewQuestion q = ReviewQuestion.builder()
                            .template(template)  // templateId → template으로 변경
                            .displayOrder(qdto.getDisplayOrder())
                            .customText(qdto.getCustomText())
                            .build();
                    review.addQuestion(q);
                }
            }
        }
    }

    @Transactional
    public void deleteReview(Long reviewId, String requesterUserId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 없습니다. id=" + reviewId));
        
        /**
         * 리뷰 삭제 권한 확인
         * 
         * 변경 사항: review.getTicket().getUserId() → review.getTicket().getUser().getId()
         * 이유: Ticket 엔티티에서 userId 필드를 User 객체로 변경했기 때문
         */
        if (!review.getTicket().getUser().getId().equals(requesterUserId)) {
            throw new SecurityException("본인 리뷰만 삭제 가능합니다.");
        }
        reviewRepository.delete(review);
    }
}