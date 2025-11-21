package com.example.record.review.dto.request;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {
    private Long ticketId;
    private String summary;
    private String keywords;
    private List<QuestionDTO> questions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuestionDTO {
        private Long templateId;
        private Integer displayOrder;
        private String customText;
    }
}