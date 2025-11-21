package com.example.record.auth.email;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /** 가장 최근 기록 하나 */
    Optional<EmailVerification> findTopByEmailOrderByCreatedAtDesc(String email);

    /** 아직 만료 안 됐고 인증 성공한 기록이 있는지 확인용 */
    boolean existsByEmailAndVerifiedIsTrueAndExpiresAtAfter(String email, LocalDateTime now);
}
