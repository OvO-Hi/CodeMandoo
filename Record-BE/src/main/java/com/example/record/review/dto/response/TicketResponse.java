package com.example.record.review.dto.response;

import com.example.record.review.entity.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 티켓 단건 응답 DTO
 *
 * 프론트엔드에서 즉시 사용할 수 있도록 필요한 필드만 노출합니다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String title;
    private String venue;
    private String genre;
    private LocalDate viewDate;
    private String imageUrl;
    private String reviewText;
    private Boolean isPublic;
    private LocalDateTime createdAt;

    public static TicketResponse from(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .title(ticket.getPerformanceTitle())
                .venue(ticket.getTheater())
                .genre(ticket.getGenre())
                .viewDate(ticket.getViewDate())
                .imageUrl(ticket.getImageUrl())
                .reviewText(ticket.getReviewText())
                .isPublic(ticket.getIsPublic())
                .createdAt(ticket.getCreatedAt())
                .build();
    }
}

