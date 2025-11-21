package com.example.record.review.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 질문 템플릿 엔티티
 * 
 * 이 엔티티는 왜 필요한가요?
 * 1. 재사용성: 같은 질문을 여러 리뷰에서 반복 사용할 수 있습니다.
 *    예: "이 공연에서 가장 인상깊었던 장면은?" 같은 질문을 모든 뮤지컬 리뷰에서 사용
 * 
 * 2. 일관성: 모든 사용자가 비슷한 질문을 받아서 리뷰의 품질을 일정하게 유지할 수 있습니다.
 * 
 * 3. 관리 편의성: 질문을 한 곳에서 관리하고, 카테고리나 장르별로 분류할 수 있습니다.
 * 
 * 4. 맞춤형 질문: 사용자의 과거 리뷰를 분석해서 개인화된 질문을 생성할 수 있습니다.
 *    예: 사용자가 자주 "배우의 연기"에 대해 언급하면, 그와 관련된 질문을 더 많이 제공
 */
@Entity
@Table(name = "questions_templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 질문 텍스트
     * 
     * 예시:
     * - "이 공연에서 가장 인상깊었던 장면은 무엇인가요?"
     * - "배우들의 연기력은 어떠했나요?"
     * - "음악과 무대 연출은 만족스러웠나요?"
     */
    @Column(name = "template_text", columnDefinition = "TEXT", nullable = false)
    private String templateText;

    /**
     * 질문 카테고리
     * 
     * 질문을 분류하는 기준입니다. 예시:
     * - "PERFORMANCE" (연기/공연)
     * - "MUSIC" (음악)
     * - "STAGE" (무대/연출)
     * - "STORY" (스토리/내용)
     * - "OVERALL" (전체적인 평가)
     * 
     * 이렇게 분류하면 사용자에게 다양한 관점의 질문을 제공할 수 있습니다.
     */
    @Column(name = "category", length = 50)
    private String category;

    /**
     * 장르별 질문 구분
     * 
     * 뮤지컬과 밴드 공연은 특성이 다르므로, 각각에 맞는 질문을 제공합니다.
     * 
     * 예시:
     * - "MUSICAL": "주인공의 성장 과정이 잘 드러났나요?"
     * - "BAND": "밴드의 무대 에너지는 어떠했나요?"
     * - "COMMON": 모든 장르에 공통으로 사용할 수 있는 질문
     */
    @Column(name = "genre", length = 50)
    private String genre;

    /**
     * 이 템플릿과 연결된 리뷰 질문들
     * 
     * @OneToMany: 하나의 템플릿이 여러 리뷰 질문에서 사용될 수 있음을 의미
     * mappedBy: ReviewQuestion 엔티티의 template 필드와 연결
     * cascade: 템플릿이 삭제되면 관련된 리뷰 질문들도 함께 삭제 (필요시)
     * fetch: LAZY로 설정해서 필요할 때만 로드 (성능 최적화)
     */
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private java.util.List<ReviewQuestion> reviewQuestions = new java.util.ArrayList<>();
}

