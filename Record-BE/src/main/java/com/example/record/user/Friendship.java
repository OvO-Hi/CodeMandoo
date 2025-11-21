package com.example.record.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

/**
 * 친구 관계 엔티티
 * 
 * 이 엔티티는 사용자들 간의 친구 관계를 관리합니다.
 * 
 * 주요 기능:
 * 1. 친구 요청 보내기
 * 2. 친구 요청 수락/거절
 * 3. 친구 목록 조회
 * 4. 친구 요청 중복 방지
 * 5. 친구 관계 상태 관리
 */
@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 친구 요청을 보낸 사용자
     * 
     * @ManyToOne: 여러 친구 관계가 하나의 사용자에게 속할 수 있음을 의미
     * FetchType.LAZY: 사용자 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     * 
     * 예시: 사용자 A가 사용자 B에게 친구 요청을 보냈다면
     * - user: 사용자 A
     * - friend: 사용자 B
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, referencedColumnName = "id")
    private User user;

    /**
     * 친구 요청을 받은 사용자
     * 
     * @ManyToOne: 여러 친구 관계가 하나의 사용자에게 속할 수 있음을 의미
     * FetchType.LAZY: 사용자 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     * 
     * 예시: 사용자 A가 사용자 B에게 친구 요청을 보냈다면
     * - user: 사용자 A (요청 보낸 사람)
     * - friend: 사용자 B (요청 받은 사람)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false, referencedColumnName = "id")
    private User friend;

    /**
     * 친구 관계 상태
     * 
     * 가능한 상태값들:
     * - "PENDING": 친구 요청이 보내졌지만 아직 수락되지 않음
     * - "ACCEPTED": 친구 요청이 수락되어 친구 관계가 성립됨
     * - "REJECTED": 친구 요청이 거절됨
     * - "BLOCKED": 차단됨 (향후 구현)
     * 
     * 왜 이렇게 설계했나요?
     * 1. 상태 추적: 친구 관계의 현재 상태를 명확히 알 수 있음
     * 2. 중복 방지: 같은 사용자에게 여러 번 요청을 보낼 수 없음
     * 3. 확장성: 향후 차단 기능 등을 쉽게 추가할 수 있음
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    /**
     * 친구 관계 생성 시간
     * 
     * @CreationTimestamp: 데이터베이스에 저장할 때 자동으로 현재 시간 설정
     * 이는 DB의 DEFAULT now()와 동일한 효과를 가집니다.
     * 
     * 사용 사례:
     * 1. 친구 요청 목록에서 시간순 정렬
     * 2. 오래된 요청 자동 정리
     * 3. 사용자 행동 패턴 분석
     * 
     * @Column: 데이터베이스의 created_at 컬럼과 매핑
     * 데이터베이스에서 DEFAULT now()로 설정되어 자동으로 현재 시간이 저장됩니다.
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 친구 관계 수정 시간
     * 
     * @UpdateTimestamp: 데이터베이스에 수정사항을 저장할 때 자동으로 현재 시간 설정
     * 이는 애플리케이션에서 수동으로 업데이트하는 것과 동일한 효과를 가집니다.
     * 
     * 사용 사례:
     * 1. 친구 요청 수락/거절 시간 추적
     * 2. 친구 관계 변경 이력 관리
     * 3. 사용자 활동 로그 분석
     * 
     * @Column: 데이터베이스의 updated_at 컬럼과 매핑
     * 상태가 변경될 때마다 애플리케이션에서 업데이트됩니다.
     */
    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;


    /**
     * 친구 관계 상태를 수락으로 변경합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 비즈니스 로직 캡슐화: 상태 변경 로직을 엔티티 내부에 포함
     * 2. 일관성: 상태 변경 시 updatedAt도 자동으로 업데이트 (@UpdateTimestamp)
     * 3. 안전성: 잘못된 상태 변경을 방지
     */
    public void accept() {
        this.status = "ACCEPTED";
        // updatedAt은 @UpdateTimestamp가 자동으로 처리
    }

    /**
     * 친구 관계 상태를 거절로 변경합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 비즈니스 로직 캡슐화: 상태 변경 로직을 엔티티 내부에 포함
     * 2. 일관성: 상태 변경 시 updatedAt도 자동으로 업데이트 (@UpdateTimestamp)
     * 3. 안전성: 잘못된 상태 변경을 방지
     */
    public void reject() {
        this.status = "REJECTED";
        // updatedAt은 @UpdateTimestamp가 자동으로 처리
    }

    /**
     * 친구 관계가 수락되었는지 확인합니다.
     * 
     * @return 친구 관계가 수락되었으면 true, 아니면 false
     */
    public boolean isAccepted() {
        return "ACCEPTED".equals(this.status);
    }

    /**
     * 친구 요청이 대기 중인지 확인합니다.
     * 
     * @return 친구 요청이 대기 중이면 true, 아니면 false
     */
    public boolean isPending() {
        return "PENDING".equals(this.status);
    }
}
