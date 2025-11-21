package com.example.record.review.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateResponse {
    private Long reviewId;
    private LocalDateTime createdAt;
}