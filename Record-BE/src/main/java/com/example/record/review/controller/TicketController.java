package com.example.record.review.controller;

import com.example.record.auth.security.AuthUser;
import com.example.record.common.ApiResponse;
import com.example.record.review.dto.request.TicketCreateRequest;
import com.example.record.review.dto.request.TicketUpdateRequest;
import com.example.record.review.dto.response.TicketResponse;
import com.example.record.review.entity.Ticket;
import com.example.record.review.repository.TicketRepository;
import com.example.record.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 티켓 CRUD 컨트롤러 (최소 기능)
 *
 * 이유: 지금까지는 프론트에서 jotai atom으로만 임시 저장했지만,
 *       실제 사용자 티켓을 RDS에 영구 저장해야 하므로 REST API를 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketRepository ticketRepository;

    /**
     * 티켓 생성
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @RequestBody TicketCreateRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            return unauthorizedResponse();
        }

        User user = authUser.getUser();
        Ticket ticket = Ticket.builder()
                .user(user)
                .performanceTitle(defaultTitle(request.getTitle()))
                .theater(request.getVenue())
                .genre(mapGenre(request.getGenre()))
                .viewDate(parseViewDate(request.getPerformedAt()))
                .imageUrl(request.getImageUrl())
                .reviewText(request.getReviewText())
                .isPublic(Boolean.TRUE.equals(request.getIsPublic()))
                .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("티켓 저장 완료: user={}, ticketId={}", user.getId(), saved.getId());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                TicketResponse.from(saved),
                "티켓이 저장되었습니다."
        ));
    }

    /**
     * 내 티켓 목록 조회
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getMyTickets(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            return unauthorizedResponse();
        }

        List<TicketResponse> tickets = ticketRepository
                .findByUser_IdOrderByCreatedAtDesc(authUser.getUser().getId())
                .stream()
                .map(TicketResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                tickets,
                "티켓 조회 성공"
        ));
    }

    /**
     * 티켓 단건 상세 조회
     *
     * 이유: 프론트가 마이페이지 외 다른 화면에서도 티켓을 새로 고쳐야 하므로
     *       특정 ID의 티켓을 다시 불러올 수 있는 API가 필요했습니다.
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            return unauthorizedResponse();
        }
        return findOwnedTicket(ticketId, authUser)
                .map(ticket -> ResponseEntity.ok(new ApiResponse<>(
                        true,
                        TicketResponse.from(ticket),
                        "티켓 조회 성공"
                )))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(new ApiResponse<>(false, null, "티켓을 찾을 수 없습니다.")));
    }

    /**
     * 티켓 수정 (부분 업데이트)
     *
     * 이유: 사용자가 제목·관람일·공개범위를 각각 수정할 수 있어야 해서
     *       전체 데이터를 다시 보내는 PUT 대신, 변경된 필드만 받아들이는 PATCH로 구성했습니다.
     */
    @PatchMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<TicketResponse>> updateTicket(
            @PathVariable Long ticketId,
            @RequestBody TicketUpdateRequest request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            return unauthorizedResponse();
        }
        Optional<Ticket> ticketOpt = findOwnedTicket(ticketId, authUser);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, null, "티켓을 찾을 수 없습니다."));
        }

        Ticket ticket = ticketOpt.get();

        if (StringUtils.hasText(request.getTitle())) {
            ticket.setPerformanceTitle(request.getTitle().trim());
        }
        if (StringUtils.hasText(request.getVenue())) {
            ticket.setTheater(request.getVenue().trim());
        }
        if (StringUtils.hasText(request.getGenre())) {
            ticket.setGenre(mapGenre(request.getGenre()));
        }
        if (StringUtils.hasText(request.getPerformedAt())) {
            ticket.setViewDate(parseViewDate(request.getPerformedAt()));
        }
        if (request.getImageUrl() != null) {
            ticket.setImageUrl(request.getImageUrl());
        }
        if (request.getReviewText() != null) {
            ticket.setReviewText(request.getReviewText());
        }
        if (request.getIsPublic() != null) {
            ticket.setIsPublic(request.getIsPublic());
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("티켓 수정 완료: user={}, ticketId={}", authUser != null ? authUser.getUser().getId() : "anonymous", ticketId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                TicketResponse.from(saved),
                "티켓이 수정되었습니다."
        ));
    }

    /**
     * 티켓 삭제
     *
     * 이유: 마이페이지에서 티켓을 삭제하면 RDS에서도 바로 제거되어야 하므로
     *       현재 로그인한 사용자의 소유권을 확인한 뒤 삭제하도록 했습니다.
     */
    @DeleteMapping("/{ticketId}")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(
            @PathVariable Long ticketId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            return unauthorizedResponse();
        }
        Optional<Ticket> ticketOpt = findOwnedTicket(ticketId, authUser);
        if (ticketOpt.isEmpty()) {
            return ResponseEntity.status(404)
                    .body(new ApiResponse<>(false, null, "티켓을 찾을 수 없습니다."));
        }

        ticketRepository.delete(ticketOpt.get());
        log.info("티켓 삭제 완료: user={}, ticketId={}", authUser != null ? authUser.getUser().getId() : "anonymous", ticketId);

        return ResponseEntity.ok(new ApiResponse<>(
                true,
                null,
                "티켓이 삭제되었습니다."
        ));
    }

    private Optional<Ticket> findOwnedTicket(Long ticketId, AuthUser authUser) {
        if (authUser == null) {
            return Optional.empty();
        }
        return ticketRepository.findById(ticketId)
                .filter(ticket -> ticket.getUser().getId().equals(authUser.getUser().getId()));
    }

    private <T> ResponseEntity<ApiResponse<T>> unauthorizedResponse() {
        // 이유: 각 API 마다 401 응답을 반복 작성하면 휴먼에러가 발생하므로
        //      공통 메서드로 분리해 재사용합니다.
        return ResponseEntity.status(401)
                .body(new ApiResponse<>(false, null, "로그인이 필요합니다."));
    }

    private String defaultTitle(String title) {
        if (StringUtils.hasText(title)) {
            return title.trim();
        }
        return "Untitled Ticket";
    }

    private String mapGenre(String genre) {
        if (!StringUtils.hasText(genre)) {
            return "COMMON";
        }
        String normalized = genre.trim().toLowerCase();
        if (normalized.contains("밴드") || normalized.contains("band")) {
            return "BAND";
        }
        if (normalized.contains("뮤지컬") || normalized.contains("연극") || normalized.contains("musical")) {
            return "MUSICAL";
        }
        return normalized.toUpperCase();
    }

    private LocalDate parseViewDate(String performedAt) {
        if (!StringUtils.hasText(performedAt)) {
            return LocalDate.now();
        }
        try {
            return LocalDateTime.parse(performedAt, DateTimeFormatter.ISO_DATE_TIME).toLocalDate();
        } catch (DateTimeParseException ex) {
            try {
                return LocalDate.parse(performedAt, DateTimeFormatter.ISO_DATE);
            } catch (DateTimeParseException ignored) {
                log.warn("공연 날짜 파싱 실패, 오늘 날짜로 저장합니다. value={}", performedAt);
                return LocalDate.now();
            }
        }
    }
}

