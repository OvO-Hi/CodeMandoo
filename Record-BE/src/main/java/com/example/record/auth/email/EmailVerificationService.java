package com.example.record.auth.email;

import com.example.record.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MailServiceT mailService;

    private static final int EXPIRE_MINUTES = 10;

    /** 6자리 숫자 코드 생성 */
    private String generateCode() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(900000) + 100000; // 100000 ~ 999999
        return String.valueOf(num);
    }

    /** 인증 코드 발송 */
    @Transactional
    public void sendCode(String email) {
        String code = generateCode();

        EmailVerification entity = EmailVerification.builder()
                .email(email)
                .code(code)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(EXPIRE_MINUTES))
                .build();

        emailVerificationRepository.save(entity);

        // 실제 이메일 발송
        mailService.sendVerificationMail(email, code);
    }

    /** 코드 검증 */
    @Transactional
    public boolean verifyCode(String email, String code) {
        var now = LocalDateTime.now();

        EmailVerification ev = emailVerificationRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElse(null);

        if (ev == null) return false;
        if (ev.isVerified()) return true; // 이미 인증했던 경우
        if (ev.getExpiresAt().isBefore(now)) return false; // 만료
        if (!ev.getCode().equals(code)) return false;      // 코드 불일치

        ev.setVerified(true);
        // 선택: 인증 성공 후, 만료 시각을 약간 연장해도 됨
        // ev.setExpiresAt(now.plusMinutes(30));
        return true;
    }

    /** 회원가입 전에, 이 이메일이 인증된 상태인지 확인하는 용도 */
    @Transactional(readOnly = true)
    public boolean isEmailVerified(String email) {
        return emailVerificationRepository
                .existsByEmailAndVerifiedIsTrueAndExpiresAtAfter(email, LocalDateTime.now());
    }
}
