package com.example.record.auth.email;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceT {

    private final JavaMailSender mailSender;

    public void sendVerificationMail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("[Re:cord] 이메일 인증 코드 안내");
        message.setText("Re:cord 회원가입을 위한 이메일 인증 코드입니다.\n\n" +
                "인증 코드: " + code + "\n\n" +
                "10분 이내에 앱에서 코드를 입력해 주세요.");
        mailSender.send(message);
    }
}
