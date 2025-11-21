package com.example.record.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 친구 관계 컨트롤러
 * 
 * 이 컨트롤러는 친구 관계 관련 HTTP 요청을 처리합니다.
 * 
 * 주요 기능:
 * 1. 친구 요청 보내기
 * 2. 친구 요청 수락/거절
 * 3. 친구 목록 조회
 * 4. 친구 요청 목록 조회
 * 5. 친구 관계 삭제
 */
@RestController
@RequestMapping("/friendships")
@RequiredArgsConstructor
public class FriendshipController {

    private final FriendshipService friendshipService;

    /**
     * 친구 요청을 보냅니다.
     * 
     * @param requesterId 요청을 보내는 사용자 ID
     * @param request 친구 요청 정보 (targetId 포함)
     * @return 친구 요청 결과
     */
    @PostMapping("/send")
    public ResponseEntity<?> sendFriendRequest(
            @RequestHeader("X-User-Id") String requesterId,
            @RequestBody Map<String, String> request) {
        try {
            String targetId = request.get("targetId");
            if (targetId == null || targetId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("대상 사용자 ID가 필요합니다.");
            }

            boolean success = friendshipService.sendFriendRequest(requesterId, targetId);
            if (success) {
                return ResponseEntity.ok("친구 요청이 성공적으로 보내졌습니다.");
            } else {
                return ResponseEntity.badRequest().body("친구 요청을 보낼 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 친구 요청을 수락합니다.
     * 
     * @param requesterId 요청을 수락하는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 요청 수락 결과
     */
    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<?> acceptFriendRequest(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable Long friendshipId) {
        try {
            boolean success = friendshipService.acceptFriendRequest(requesterId, friendshipId);
            if (success) {
                return ResponseEntity.ok("친구 요청이 성공적으로 수락되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("친구 요청을 수락할 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 친구 요청을 거절합니다.
     * 
     * @param requesterId 요청을 거절하는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 요청 거절 결과
     */
    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<?> rejectFriendRequest(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable Long friendshipId) {
        try {
            boolean success = friendshipService.rejectFriendRequest(requesterId, friendshipId);
            if (success) {
                return ResponseEntity.ok("친구 요청이 성공적으로 거절되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("친구 요청을 거절할 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 친구 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 친구 목록
     */
    @GetMapping("/{userId}/friends")
    public ResponseEntity<?> getFriends(@PathVariable String userId) {
        try {
            List<Friendship> friends = friendshipService.getFriends(userId);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 보낸 친구 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 보낸 친구 요청 목록
     */
    @GetMapping("/{userId}/sent-requests")
    public ResponseEntity<?> getSentFriendRequests(@PathVariable String userId) {
        try {
            List<Friendship> sentRequests = friendshipService.getSentFriendRequests(userId);
            return ResponseEntity.ok(sentRequests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 받은 친구 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 받은 친구 요청 목록
     */
    @GetMapping("/{userId}/received-requests")
    public ResponseEntity<?> getReceivedFriendRequests(@PathVariable String userId) {
        try {
            List<Friendship> receivedRequests = friendshipService.getReceivedFriendRequests(userId);
            return ResponseEntity.ok(receivedRequests);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 친구 관계를 삭제합니다.
     * 
     * @param requesterId 친구 관계를 삭제하려는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 관계 삭제 결과
     */
    @DeleteMapping("/{friendshipId}")
    public ResponseEntity<?> deleteFriendship(
            @RequestHeader("X-User-Id") String requesterId,
            @PathVariable Long friendshipId) {
        try {
            boolean success = friendshipService.deleteFriendship(requesterId, friendshipId);
            if (success) {
                return ResponseEntity.ok("친구 관계가 성공적으로 삭제되었습니다.");
            } else {
                return ResponseEntity.badRequest().body("친구 관계를 삭제할 수 없습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 친구 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 친구 수
     */
    @GetMapping("/{userId}/friend-count")
    public ResponseEntity<?> getFriendCount(@PathVariable String userId) {
        try {
            long friendCount = friendshipService.getFriendCount(userId);
            return ResponseEntity.ok(Map.of("friendCount", friendCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }

    /**
     * 대기 중인 친구 요청 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 대기 중인 친구 요청 수
     */
    @GetMapping("/{userId}/pending-count")
    public ResponseEntity<?> getPendingFriendRequestCount(@PathVariable String userId) {
        try {
            long pendingCount = friendshipService.getPendingFriendRequestCount(userId);
            return ResponseEntity.ok(Map.of("pendingCount", pendingCount));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류가 발생했습니다.");
        }
    }
}

