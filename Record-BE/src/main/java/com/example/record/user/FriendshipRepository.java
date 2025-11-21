package com.example.record.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 친구 관계 레포지토리
 * 
 * 이 레포지토리는 친구 관계를 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 친구 요청 보내기
 * 2. 친구 요청 수락/거절
 * 3. 친구 목록 조회
 * 4. 친구 요청 목록 조회
 * 5. 중복 요청 방지
 * 
 * 변경 사항:
 * - Friendship 엔티티의 필드명에 맞춰 수정 (user/friend 필드 사용)
 * - 잘못된 메서드 시그니처 수정
 * - 실제 사용되는 메서드들만 유지
 */
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    /**
     * 특정 사용자가 보낸 친구 요청을 조회합니다.
     *
     * @param userId 요청을 보낸 사용자 ID
     * @return 해당 사용자가 보낸 친구 요청 목록
     */
    List<Friendship> findByUser_Id(String userId);

    /**
     * 특정 사용자에게 온 친구 요청을 조회합니다.
     *
     * @param friendId 요청을 받은 사용자 ID
     * @return 해당 사용자에게 온 친구 요청 목록
     */
    List<Friendship> findByFriend_Id(String friendId);

    /**
     * 특정 사용자 간의 친구 관계를 조회합니다.
     * (user-friend 또는 friend-user 관계 모두 포함)
     *
     * @param userId1 첫 번째 사용자 ID
     * @param friendId1 첫 번째 사용자의 친구 ID
     * @param userId2 두 번째 사용자 ID
     * @param friendId2 두 번째 사용자의 친구 ID
     * @return 두 사용자 간의 친구 관계 (존재하지 않으면 Optional.empty())
     */
    Optional<Friendship> findByUser_IdAndFriend_IdOrUser_IdAndFriend_Id(String userId1, String friendId1, String userId2, String friendId2);

    /**
     * 특정 사용자 간의 친구 요청이 존재하는지 확인합니다.
     * (user-friend 또는 friend-user 관계 모두 포함)
     *
     * @param userId 사용자 ID
     * @param friendId 친구 ID
     * @return 친구 요청이 존재하면 true, 아니면 false
     */
    boolean existsByUser_IdAndFriend_Id(String userId, String friendId);

    /**
     * 특정 사용자의 친구 목록을 조회합니다. (상태가 ACCEPTED인 경우)
     *
     * @param userId1 첫 번째 사용자 ID
     * @param status1 첫 번째 상태
     * @param userId2 두 번째 사용자 ID
     * @param status2 두 번째 상태
     * @return 해당 사용자의 친구 목록
     */
    List<Friendship> findByUser_IdAndStatusOrFriend_IdAndStatus(String userId1, String status1, String userId2, String status2);
}

