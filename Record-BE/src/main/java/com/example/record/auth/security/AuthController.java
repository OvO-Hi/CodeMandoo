package com.example.record.auth.security;

import com.example.record.auth.jwt.JwtUtil;
import com.example.record.auth.jwt.TokenResponse;
import com.example.record.auth.login.SignupRequest;
import com.example.record.auth.login.LoginRequest;
import com.example.record.auth.email.EmailSendRequest;
import com.example.record.auth.email.EmailVerifyRequest;
import com.example.record.auth.email.EmailVerificationService;
import com.example.record.common.ApiResponse;
import com.example.record.user.User;
import com.example.record.user.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;   // ✅ 추가

    // =========================
    // 이메일 인증 관련 엔드포인트
    // =========================

    /** 이메일로 6자리 인증 코드 보내기 */
    @PostMapping("/email/send-code")
    public ResponseEntity<ApiResponse<?>> sendEmailCode(
            @Valid @RequestBody EmailSendRequest request) {

        // 이미 가입된 이메일이면 막을지 말지는 정책에 따라 선택.
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "이미 가입된 이메일입니다.")
            );
        }

        emailVerificationService.sendCode(request.getEmail());

        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "인증 코드가 이메일로 발송되었습니다.")
        );
    }

    /** 이메일 + 코드로 인증 완료 처리 */
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<?>> verifyEmailCode(
            @Valid @RequestBody EmailVerifyRequest request) {

        boolean ok = emailVerificationService.verifyCode(request.getEmail(), request.getCode());

        if (!ok) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "인증 코드가 올바르지 않거나 만료되었습니다.")
            );
        }

        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "이메일 인증이 완료되었습니다.")
        );
    }

    // =========================
    // 기존 회원가입 / 로그인
    // =========================

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {

        // 1) 이메일 중복
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "이미 사용 중인 이메일입니다.")
            );
        }

        // 2) 아이디 중복
        if (userRepository.existsById(request.getId())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "이미 사용 중인 아이디입니다.")
            );
        }

        // 3) 이메일 인증 여부 체크 🔥
        if (!emailVerificationService.isEmailVerified(request.getEmail())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "이메일 인증을 먼저 완료해 주세요.")
            );
        }

        // 4) 실제 유저 생성
        User user = User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role("USER")
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        TokenResponse tokenResponse = new TokenResponse(
                token, "Bearer", jwtUtil.getExpirationMs(), user.getRole()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, tokenResponse, "회원가입 성공")
        );
    }

    /** 로그인 */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("아이디를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null, "비밀번호가 일치하지 않습니다.")
            );
        }

        String token = jwtUtil.generateToken(user.getId(), user.getRole());

        TokenResponse tokenResponse = new TokenResponse(
                token, "Bearer", jwtUtil.getExpirationMs(), user.getRole()
        );

        return ResponseEntity.ok(
                new ApiResponse<>(true, tokenResponse, "로그인 성공")
        );
    }

    /** 로그인한 사용자 정보 조회 */

    //    /** 로그인한 사용자 정보 조회 */
//    @GetMapping("/me")
//    public ResponseEntity<ApiResponse<?>> getCurrentUser(
//            @org.springframework.security.core.annotation.AuthenticationPrincipal AuthUser authUser) {
//
//        if (authUser == null) {
//            return ResponseEntity.badRequest().body(
//                    new ApiResponse<>(false, null, "인증된 사용자 정보를 찾을 수 없습니다.")
//            );
//        }
//
//        User user = authUser.getUser();
//
//        UserResponse userResponse = new UserResponse(
//                user.getId(),
//                user.getEmail(),
//                user.getNickname(),
//                user.getRole()
//        );
//
//        return ResponseEntity.ok(
//                new ApiResponse<>(true, userResponse, "사용자 정보 조회 성공")
//        );
//    }



    /** 로그아웃 */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout() {
        return ResponseEntity.ok(
                new ApiResponse<>(true, null, "로그아웃 성공")
        );
    }

    public record UserResponse(
            String id,
            String email,
            String nickname,
            String role
    ) {}
}
