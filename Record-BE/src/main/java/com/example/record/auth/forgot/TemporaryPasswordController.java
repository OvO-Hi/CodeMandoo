package com.example.record.auth.forgot;

import com.example.record.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/forgot")
@RequiredArgsConstructor
@Validated
public class TemporaryPasswordController {

    private final TemporaryPasswordService temporaryPasswordService;

    @PostMapping("/temporary-password")
    public ResponseEntity<ApiResponse<?>> sendTempPassword(@Valid @RequestBody TempPwRequest req) {
        try {
            temporaryPasswordService.issueAndSend(req.getEmail());

            return ResponseEntity.ok(
                    new ApiResponse<>(true, null, "임시 비밀번호가 이메일로 발급되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            // 서비스에서 "가입된 이메일을 찾을 수 없습니다." 던진 경우
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, e.getMessage())
            );
        }
    }

    @Data
    public static class TempPwRequest {
        @NotBlank
        @Email
        private String email;
    }
}
