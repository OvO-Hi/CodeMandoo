// com.example.record.review.dto.request.ReviewUpdateRequest
package com.example.record.review.dto.request;

import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewUpdateRequest {
    private String summary;   // null이면 변경 안 함
    private String keywords;  // null이면 변경 안 함
    private List<QuestionDTO> questions; // null이면 질문은 변경 안 함, 빈배열이면 전부 삭제

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class QuestionDTO {
        private Long templateId;
        private Integer displayOrder;
        private String customText;
    }
}
