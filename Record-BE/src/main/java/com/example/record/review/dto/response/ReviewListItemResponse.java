package com.example.record.review.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewListItemResponse {
    private Long reviewId;
    private Long ticketId;
    private String summary;
    private String keywords;
    private String ticketImageUrl;
    private String performanceTitle;
    private LocalDateTime createdAt;
}