// com.example.record.review.dto.response.ReviewListItemResponse
package com.example.record.review.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewListItemResponse {
    private Long reviewId;
    private Long ticketId;
    private String summary;
    private String keywords;
    private String ticketImageUrl;      // 썸네일용
    private String performanceTitle;    // 선택: PerformanceInfo.title 가져오면 채움
    private LocalDateTime createdAt;
}
