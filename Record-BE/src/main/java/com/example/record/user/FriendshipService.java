package com.example.record.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 서비스
 * 
 * 이 서비스는 친구 관계 관련 비즈니스 로직을 처리합니다.
 * 
 * 주요 기능:
 * 1. 친구 요청 보내기
 * 2. 친구 요청 수락/거절
 * 3. 친구 목록 조회
 * 4. 친구 요청 목록 조회
 * 5. 중복 요청 방지
 * 6. 친구 관계 삭제
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendshipService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;

    /**
     * 친구 요청을 보냅니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 중복 요청 방지: 같은 사용자에게 여러 번 요청을 보낼 수 없음
     * 2. 자기 자신 요청 방지: 자신에게 친구 요청을 보낼 수 없음
     * 3. 존재하지 않는 사용자 요청 방지: 실제 사용자에게만 요청 가능
     * 
     * @param requesterId 요청을 보내는 사용자 ID
     * @param targetId 요청을 받을 사용자 ID
     * @return 친구 요청이 성공적으로 보내졌으면 true, 아니면 false
     * @throws IllegalArgumentException 잘못된 요청인 경우
     */
    @Transactional
    public boolean sendFriendRequest(String requesterId, String targetId) {
        // 자기 자신에게 친구 요청을 보낼 수 없음
        if (requesterId.equals(targetId)) {
            throw new IllegalArgumentException("자기 자신에게 친구 요청을 보낼 수 없습니다.");
        }

        // 요청을 받을 사용자가 존재하는지 확인
        User targetUser = userRepository.findById(targetId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 이미 친구 요청이 있는지 확인
        if (friendshipRepository.existsByUser_IdAndFriend_Id(requesterId, targetId) ||
                friendshipRepository.existsByUser_IdAndFriend_Id(targetId, requesterId)) {
            throw new IllegalArgumentException("이미 친구 요청이 있거나 친구 관계가 존재합니다.");
        }

        // 친구 요청 생성
        User requesterUser = userRepository.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Friendship friendship = Friendship.builder()
                .user(requesterUser)
                .friend(targetUser)
                .status("PENDING")
                .build();

        friendshipRepository.save(friendship);
        return true;
    }

    /**
     * 친구 요청을 수락합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 권한 확인: 요청을 받은 사용자만 수락할 수 있음
     * 2. 상태 확인: 대기 중인 요청만 수락 가능
     * 3. 양방향 친구 관계: 수락 시 양쪽 모두 친구가 됨
     * 
     * @param requesterId 요청을 수락하는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 요청이 성공적으로 수락되었으면 true, 아니면 false
     * @throws IllegalArgumentException 잘못된 요청인 경우
     */
    @Transactional
    public boolean acceptFriendRequest(String requesterId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        // 요청을 받은 사용자만 수락할 수 있음
        if (!friendship.getFriend().getId().equals(requesterId)) {
            throw new IllegalArgumentException("본인의 친구 요청만 수락할 수 있습니다.");
        }

        // 대기 중인 요청만 수락 가능
        if (!friendship.isPending()) {
            throw new IllegalArgumentException("대기 중인 친구 요청만 수락할 수 있습니다.");
        }

        // 친구 요청 수락
        friendship.accept();
        friendshipRepository.save(friendship);
        return true;
    }

    /**
     * 친구 요청을 거절합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 권한 확인: 요청을 받은 사용자만 거절할 수 있음
     * 2. 상태 확인: 대기 중인 요청만 거절 가능
     * 3. 명확한 거절: 거절 상태로 명확히 표시
     * 
     * @param requesterId 요청을 거절하는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 요청이 성공적으로 거절되었으면 true, 아니면 false
     * @throws IllegalArgumentException 잘못된 요청인 경우
     */
    @Transactional
    public boolean rejectFriendRequest(String requesterId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 요청입니다."));

        // 요청을 받은 사용자만 거절할 수 있음
        if (!friendship.getFriend().getId().equals(requesterId)) {
            throw new IllegalArgumentException("본인의 친구 요청만 거절할 수 있습니다.");
        }

        // 대기 중인 요청만 거절 가능
        if (!friendship.isPending()) {
            throw new IllegalArgumentException("대기 중인 친구 요청만 거절할 수 있습니다.");
        }

        // 친구 요청 거절
        friendship.reject();
        friendshipRepository.save(friendship);
        return true;
    }

    /**
     * 특정 사용자의 친구 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 친구 목록
     */
    public List<Friendship> getFriends(String userId) {
        return friendshipRepository.findByUser_IdAndStatusOrFriend_IdAndStatus(userId, "ACCEPTED", userId, "ACCEPTED");
    }

    /**
     * 특정 사용자가 보낸 친구 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자가 보낸 친구 요청 목록
     */
    public List<Friendship> getSentFriendRequests(String userId) {
        return friendshipRepository.findByUser_Id(userId);
    }

    /**
     * 특정 사용자가 받은 친구 요청 목록을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자가 받은 친구 요청 목록
     */
    public List<Friendship> getReceivedFriendRequests(String userId) {
        return friendshipRepository.findByFriend_Id(userId);
    }

    /**
     * 두 사용자 간의 친구 관계를 조회합니다.
     * 
     * @param userId 첫 번째 사용자 ID
     * @param friendId 두 번째 사용자 ID
     * @return 두 사용자 간의 친구 관계 (없으면 Optional.empty())
     */
    public Optional<Friendship> getFriendship(String userId, String friendId) {
        return friendshipRepository.findByUser_IdAndFriend_IdOrUser_IdAndFriend_Id(userId, friendId, userId, friendId);
    }

    /**
     * 친구 관계를 삭제합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 권한 확인: 친구 관계의 당사자만 삭제할 수 있음
     * 2. 친구 관계 해제: 양쪽 모두 친구 관계가 해제됨
     * 3. 데이터 정리: 불필요한 친구 관계 정리
     * 
     * @param requesterId 친구 관계를 삭제하려는 사용자 ID
     * @param friendshipId 친구 관계 ID
     * @return 친구 관계가 성공적으로 삭제되었으면 true, 아니면 false
     * @throws IllegalArgumentException 잘못된 요청인 경우
     */
    @Transactional
    public boolean deleteFriendship(String requesterId, Long friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 친구 관계입니다."));

        // 친구 관계의 당사자만 삭제할 수 있음
        if (!friendship.getUser().getId().equals(requesterId) && 
            !friendship.getFriend().getId().equals(requesterId)) {
            throw new IllegalArgumentException("본인의 친구 관계만 삭제할 수 있습니다.");
        }

        // 친구 관계 삭제
        friendshipRepository.delete(friendship);
        return true;
    }

    /**
     * 특정 사용자의 친구 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 친구 수
     */
    public long getFriendCount(String userId) {
        return friendshipRepository.findByUser_IdAndStatusOrFriend_IdAndStatus(userId, "ACCEPTED", userId, "ACCEPTED").size();
    }

    /**
     * 특정 사용자가 받은 대기 중인 친구 요청 수를 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자가 받은 대기 중인 친구 요청 수
     */
    public long getPendingFriendRequestCount(String userId) {
        return friendshipRepository.findByFriend_Id(userId).stream()
                .filter(friendship -> "PENDING".equals(friendship.getStatus()))
                .count();
    }
}

