package com.example.record.review.entity;

import com.example.record.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 사용자 맞춤 질문 엔티티
 * 
 * 역할: 사용자의 과거 후기를 분석하여 생성된 개인화된 질문들을 저장
 * 
 * 왜 이 엔티티가 필요한가요?
 * 1. 개인화: 사용자가 자주 언급하는 주제와 관련된 질문 제공
 * 2. 학습: 사용자의 후기 작성 패턴을 학습하여 더 나은 질문 생성
 * 3. 재사용: 한 번 생성된 질문을 여러 번 사용할 수 있음
 * 
 * 생성 시점:
 * - 사용자의 후기가 3개, 6개, 9개... 이런 식으로 3개씩 늘어날 때마다
 * - 새로 추가된 3개 후기를 분석하여 관련 질문들을 생성
 */
@Entity
@Table(name = "user_custom_questions", indexes = {
    @Index(name = "idx_user_custom_questions_user_id", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCustomQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 질문을 소유한 사용자
     * 
     * @ManyToOne: 여러 맞춤 질문이 하나의 사용자에게 속할 수 있음을 의미
     * @JoinColumn: 데이터베이스의 user_id 컬럼과 연결
     * FetchType.LAZY: 사용자 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    /**
     * 질문 카테고리
     * 
     * 역할: 질문을 분류하는 기준
     * 
     * 예시:
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
     * 역할: 뮤지컬과 밴드 공연은 특성이 다르므로, 각각에 맞는 질문을 제공
     * 
     * 예시:
     * - "MUSICAL": "주인공의 성장 과정이 잘 드러났나요?"
     * - "BAND": "밴드의 무대 에너지는 어떠했나요?"
     * - "COMMON": 모든 장르에 공통으로 사용할 수 있는 질문
     */
    @Column(name = "genre", length = 50)
    private String genre;

    /**
     * 질문 텍스트
     * 
     * 역할: 사용자에게 표시될 실제 질문 내용
     * 
     * 예시:
     * - "이 공연에서 가장 인상깊었던 장면은 무엇인가요?"
     * - "배우들의 연기력은 어떠했나요?"
     * - "음악과 무대 연출은 만족스러웠나요?"
     */
    @Column(name = "template_text", columnDefinition = "TEXT", nullable = false)
    private String templateText;

    /**
     * 질문 생성 시간
     * 
     * @CreationTimestamp: 데이터베이스에 저장할 때 자동으로 현재 시간 설정
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
}

