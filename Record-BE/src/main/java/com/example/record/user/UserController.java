package com.example.record.user;

import com.example.record.auth.security.AuthUser;
import com.example.record.common.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;

    // ────────────────────────────────
    // Request DTO
    // ────────────────────────────────
    @Getter
    @Setter
    public static class UpdateProfileRequest {
        private String nickname;
        private String email;
        private Boolean isAccountPrivate;
    }

    @Getter
    @Setter
    public static class DeleteAccountRequest {
        @NotBlank
        private String password;
    }

    // ────────────────────────────────
    // 1. 내 프로필 조회
    // ────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<?>> getMyProfile(
            @AuthenticationPrincipal AuthUser authUser) {

        if (authUser == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "인증된 사용자 정보를 찾을 수 없습니다.")
            );
        }

        User user = authUser.getUser();
        return ResponseEntity.ok(
                new ApiResponse<>(true, new UserProfileResponse(user), "프로필 조회 성공")
        );
    }

    // ────────────────────────────────
    // 2. 프로필 정보 변경 (닉네임/이메일/비공개 여부)
    // ────────────────────────────────
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<?>> updateProfile(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody UpdateProfileRequest req
    ) {

        if (authUser == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "인증된 사용자 정보를 찾을 수 없습니다.")
            );
        }

        User updated = userService.updateProfile(authUser.getUser(), req);

        return ResponseEntity.ok(
                new ApiResponse<>(true, new UserProfileResponse(updated), "프로필이 수정되었습니다.")
        );
    }

    // ────────────────────────────────
    // 3. 프로필 이미지 업로드
    // ────────────────────────────────
    @PutMapping(
            value = "/me/profile-image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ApiResponse<?>> uploadProfileImage(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestPart("file") MultipartFile file
    ) {

        if (authUser == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "인증된 사용자 정보를 찾을 수 없습니다.")
            );
        }

        User updated = userService.updateProfileImage(authUser.getUser(), file);
        return ResponseEntity.ok(
                new ApiResponse<>(true, new UserProfileResponse(updated), "프로필 이미지가 변경되었습니다.")
        );
    }

    // ────────────────────────────────
    // 4. 회원탈퇴
    // ────────────────────────────────
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<?>> deleteAccount(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody(required = false) DeleteAccountRequest req
    ) {

        if (authUser == null) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "인증된 사용자 정보를 찾을 수 없습니다.")
            );
        }

        User user = authUser.getUser();

        if (req != null && req.getPassword() != null) {
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                        new ApiResponse<>(false, null, "비밀번호가 일치하지 않습니다.")
                );
            }
        }

        userRepository.delete(user);
        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "회원탈퇴가 완료되었습니다.")
        );
    }

    // ────────────────────────────────
    // Response DTO
    // ────────────────────────────────
    public record UserProfileResponse(
            String id,
            String nickname,
            String email,
            String profileImage,
            Boolean isAccountPrivate,
            String createdAt,
            String updatedAt
    ) {
        public UserProfileResponse(User user) {
            this(
                    user.getId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getProfileImage(),
                    user.getIsAccountPrivate(),

                    user.getCreatedAt() != null ? user.getCreatedAt().toString() : null,
                    user.getUpdatedAt() != null ? user.getUpdatedAt().toString() : null
            );
        }
    }
}
