package com.example.record.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_reviews_ticket_id", columnList = "ticket_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String keywords;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewQuestion> questions = new ArrayList<>();

    /**
     * 이 리뷰와 연결된 생성된 이미지들
     * 
     * @OneToMany: 하나의 리뷰가 여러 생성된 이미지를 가질 수 있음을 의미
     * mappedBy: GeneratedImageUrl 엔티티의 review 필드와 연결
     * cascade: 리뷰가 삭제되면 관련된 모든 이미지도 함께 삭제
     * orphanRemoval: 부모가 없는 이미지들을 자동으로 삭제
     * fetch: LAZY로 설정해서 필요할 때만 로드 (성능 최적화)
     * 
     * 사용 사례:
     * 1. 이미지 생성 히스토리: 사용자가 여러 번 이미지를 생성할 수 있음
     * 2. 이미지 선택: 여러 이미지 중에서 최종 선택
     * 3. 이미지 관리: 생성, 삭제, 선택 상태 변경
     */
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GeneratedImageUrl> generatedImages = new ArrayList<>();


    public void addQuestion(ReviewQuestion q) {
        q.setReview(this);
        questions.add(q);
    }

    /**
     * 생성된 이미지를 리뷰에 추가합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 양방향 관계 관리: 이미지가 리뷰를 참조하고, 리뷰도 이미지를 참조
     * 2. 데이터 일관성: 이미지 생성 시 자동으로 리뷰와 연결
     * 3. 편의성: 개발자가 매번 양방향 관계를 설정할 필요 없음
     * 
     * @param image 추가할 생성된 이미지
     */
    public void addGeneratedImage(GeneratedImageUrl image) {
        image.setReview(this);
        generatedImages.add(image);
    }
}