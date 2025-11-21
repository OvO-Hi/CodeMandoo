package com.example.record.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 생성된 이미지 URL 엔티티
 * 
 * 이 엔티티는 왜 필요한가요?
 * 1. 이미지 생성 히스토리 관리: 사용자가 여러 번 이미지를 생성할 수 있으므로,
 *    모든 생성 결과를 저장해서 나중에 비교할 수 있게 합니다.
 * 
 * 2. 사용자 선택 관리: 사용자가 여러 이미지 중에서 최종 선택한 것을 표시할 수 있습니다.
 * 
 * 3. 이미지 스타일 추적: 어떤 스타일로 이미지를 생성했는지 기록해서
 *    사용자 선호도를 파악할 수 있습니다.
 * 
 * 4. 데이터 분석: 어떤 스타일의 이미지가 더 인기 있는지 분석할 수 있습니다.
 */
@Entity
@Table(name = "generated_image_url", indexes = {
    @Index(name = "idx_generated_images_review_id", columnList = "review_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GeneratedImageUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 이미지가 속한 리뷰
     * 
     * @ManyToOne: 여러 이미지가 하나의 리뷰에 속할 수 있음을 의미
     * FetchType.LAZY: 리뷰 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     * 
     * 예시: 하나의 리뷰에 대해 여러 스타일의 이미지를 생성할 수 있습니다.
     * - "로맨틱 스타일" 이미지
     * - "액션 스타일" 이미지  
     * - "드라마 스타일" 이미지
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /**
     * 생성된 이미지의 URL
     * 
     * DALL-E나 다른 AI 이미지 생성 서비스에서 반환되는 이미지 URL입니다.
     * 
     * 예시: "https://oaidalleapiprodscus.blob.core.windows.net/private/..."
     * 
     * 왜 TEXT 타입을 사용하나요?
     * - URL이 매우 길 수 있기 때문입니다 (400자 이상)
     * - VARCHAR(400)보다 TEXT가 더 안전합니다
     */
    @Column(name = "image_url", columnDefinition = "TEXT", nullable = false)
    private String imageUrl;

    /**
     * 이미지 생성 스타일
     * 
     * 어떤 스타일로 이미지를 생성했는지 기록합니다.
     * 
     * 예시:
     * - "ROMANTIC": 로맨틱한 분위기
     * - "ACTION": 액션/역동적인 분위기
     * - "DRAMA": 드라마틱한 분위기
     * - "COMEDY": 코미디/유쾌한 분위기
     * - "MUSICAL": 뮤지컬 특유의 화려함
     * - "BAND": 밴드 공연의 에너지
     * 
     * 이 정보는 사용자 선호도 분석과 개인화된 이미지 추천에 사용됩니다.
     */
    @Column(name = "style", length = 30)
    private String style;

    /**
     * 이미지 생성 시간
     * 
     * @CreationTimestamp: 데이터베이스에 저장할 때 자동으로 현재 시간 설정
     * 이는 DB의 DEFAULT now()와 동일한 효과를 가집니다.
     * 
     * @Column: 데이터베이스의 created_at 컬럼과 매핑
     * 데이터베이스에서 DEFAULT now()로 설정되어 자동으로 현재 시간이 저장됩니다.
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 사용자가 이 이미지를 최종 선택했는지 여부
     * 
     * 사용자가 여러 이미지 중에서 최종적으로 선택한 이미지를 표시합니다.
     * 
     * 사용 사례:
     * 1. 리뷰 상세 페이지에서 선택된 이미지 강조 표시
     * 2. 사용자 프로필에서 대표 이미지로 사용
     * 3. 친구들에게 공유할 때 선택된 이미지 사용
     * 
     * 기본값: false (선택되지 않음)
     * 사용자가 선택하면 true로 변경됩니다.
     */
    @Column(name = "is_selected")
    @Builder.Default
    private Boolean isSelected = false;

}
