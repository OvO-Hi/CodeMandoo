package com.example.record.auth.forgot;

import com.example.record.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IDRecoveryService {

    private final UserRepository userRepository;
    private final MailService mailService;

    /** 1) 아이디 찾기: 이메일로 아이디 발송 */
    @Transactional(readOnly = true)
    public void sendLoginIdByEmail(ForgotIdRequest req) {
        userRepository.findByEmail(req.getEmail()).ifPresent(user -> {
            String loginId = user.getId();

            String subject = "[Re:cord] 가입 아이디 안내";
            String body = """
                    안녕하세요.
                    요청하신 가입 아이디를 안내드립니다.

                    아이디: %s

                    본 메일은 요청에 의해 발송되었습니다.
                    본인이 요청하지 않았다면 문의해 주세요.
                    """.formatted(loginId);

            mailService.send(user.getEmail(), subject, body);
        });
        // 존재하지 않는 이메일이어도 같은 응답 → 보안상 OK
    }
}
