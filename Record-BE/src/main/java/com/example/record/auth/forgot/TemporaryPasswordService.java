package com.example.record.auth.forgot;

import com.example.record.user.User;
import com.example.record.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemporaryPasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    /** 이메일로 임시 비번 생성/저장/발송 */
    @Transactional
    public void issueAndSend(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("가입된 이메일을 찾을 수 없습니다."));

        String temp = TempPasswordGenerator.generate(12); // 8자 이상 숫자+영문 혼합
        user.setPassword(passwordEncoder.encode(temp));    // 기존 비번 덮어쓰기

        // 메일 본문은 앱 기준으로 안내
        String subject = "[Re:cord] 임시 비밀번호가 발급되었습니다";
        String body = """
                안녕하세요. Re:cord 입니다.

                아래 임시 비밀번호로 로그인하신 후
                마이페이지 > 비밀번호 변경에서 새 비밀번호로 교체해 주세요.

                임시 비밀번호: %s

                안전을 위해 다른 사람과 공유하지 마세요.
                """.formatted(temp);

        mailService.send(email, subject, body);
    }
}
