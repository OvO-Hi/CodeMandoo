package com.example.record.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_questions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 질문이 속한 리뷰
     * 
     * @ManyToOne: 여러 질문이 하나의 리뷰에 속할 수 있음을 의미
     * FetchType.LAZY: 리뷰 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /**
     * 질문 템플릿과의 관계
     * 
     * 왜 이렇게 변경했나요?
     * 1. 데이터 무결성: 존재하지 않는 템플릿 ID로 질문을 만들 수 없게 됩니다.
     * 2. 조인 쿼리: 템플릿 정보와 함께 질문을 조회할 수 있습니다.
     * 3. 객체지향 설계: question.getTemplate().getTemplateText()로 자연스럽게 접근 가능
     * 
     * 변경 사항:
     * - templateId(Long) → template(QuestionTemplate 객체)
     * - @Column → @ManyToOne으로 변경
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", nullable = false)
    private QuestionTemplate template;

    /**
     * 질문 표시 순서
     * 
     * 리뷰 작성 화면에서 질문들이 어떤 순서로 나타날지 결정합니다.
     * 예: 1번 질문, 2번 질문, 3번 질문...
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    /**
     * 사용자 정의 질문 텍스트
     * 
     * 템플릿 질문을 그대로 사용하지 않고, 사용자가 직접 수정한 질문이 있을 때 저장됩니다.
     * 
     * 예시:
     * - 템플릿: "이 공연에서 가장 인상깊었던 장면은 무엇인가요?"
     * - 사용자 수정: "이 뮤지컬에서 가장 감동받았던 노래는 무엇인가요?"
     * 
     * 이 경우 customText에 사용자가 수정한 내용이 저장됩니다.
     */
    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String customText;
}