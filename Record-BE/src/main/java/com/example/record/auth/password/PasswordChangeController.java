package com.example.record.auth.password;

import com.example.record.auth.security.AuthUser;
import com.example.record.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    @PostMapping("/change")
    public ResponseEntity<ApiResponse<?>> changePassword(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody PasswordChangeRequest req
    ) {
        // 로그인 안 된 경우
        if (authUser == null) {
            return ResponseEntity
                    .status(401)
                    .body(new ApiResponse<>(false, null, "로그인이 필요합니다."));
        }

        try {
            passwordChangeService.changePassword(authUser.getUser(), req);
            return ResponseEntity.ok(
                    new ApiResponse<>(true, null, "비밀번호가 변경되었습니다.")
            );
        } catch (IllegalArgumentException e) {
            // 현재 비밀번호가 틀린 경우 등
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, e.getMessage())
            );
        }
    }
}
