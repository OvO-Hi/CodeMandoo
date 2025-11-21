package com.example.record.review.dto.request;

import lombok.Getter;
import lombok.Setter;

/**
 * 티켓 수정 요청 DTO
 *
 * 이유: 프론트엔드에서 제목, 공연장, 공개 여부 등의 일부 필드만 수정할 수 있으므로
 *       PATCH 용도로 모든 필드를 Optional 하게 정의합니다.
 */
@Getter
@Setter
public class TicketUpdateRequest {

    /** 공연 제목 */
    private String title;

    /** 공연장 (venue) */
    private String venue;

    /** 장르 */
    private String genre;

    /** 관람 일시 (ISO8601 문자열) */
    private String performedAt;

    /** AI 이미지 등의 URL */
    private String imageUrl;

    /** 후기 텍스트 */
    private String reviewText;

    /** 공개 여부 */
    private Boolean isPublic;
}

