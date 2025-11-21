package com.example.record.auth.forgot;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class IDRecoveryController {

    private final IDRecoveryService service;

    /** 1) 아이디 찾기: 이메일로 아이디 전송 */
    @PostMapping("/forgot-id")
    public ResponseEntity<?> forgotId(@RequestBody @Valid ForgotIdRequest req) {
        service.sendLoginIdByEmail(req);
        // 존재 유무를 노출하지 않기 위해 항상 동일 메시지
        return ResponseEntity.ok("해당 이메일로 안내 메일을 확인해 주세요.");
    }

    // 없앰
    // @PostMapping("/forgot-password") ...
    // @PostMapping("/reset-password") ...
}
