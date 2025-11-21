package com.example.record;


import com.example.record.user.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class DBTestController {

    @GetMapping("/protected")
    public String protectedEndpoint(@AuthenticationPrincipal User user) {
        return "안녕하세요, " + user.getNickname() + "님! 인증이 완료되었습니다.";
    }
}
