package com.example.record.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 티켓 생성 요청 DTO
 *
 * 프론트엔드에서 입력한 공연 정보 + 리뷰 텍스트를 RDS에 저장하기 위한 요청 본문입니다.
 * - performedAt: ISO 8601 문자열 (예: 2025-11-21T12:30:00.000Z)
 * - genre: "밴드", "뮤지컬" 등 프론트 표현 그대로 전달되며, 서버에서 영문 코드로 매핑합니다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketCreateRequest {

    private String title;
    private String venue;
    private String genre;
    private String performedAt;
    private String imageUrl;
    private String reviewText;
    private Boolean isPublic;

    // 선택 필드 (향후 확장 대비)
    private String seat;
    private String bookingSite;
}

