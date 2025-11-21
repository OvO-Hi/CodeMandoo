package com.example.record.STTorText.stt;

import com.example.record.STTorText.entity.Transcription;
import com.example.record.STTorText.entity.TranscriptionRepository;
import com.example.record.auth.security.AuthUser;
import com.example.record.common.ApiResponse;
import com.example.record.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stt")
public class SttController {

    private final WhisperService whisperService;
    private final SttService sttService;
    private final TranscriptionRepository repo;

    @PostMapping("/transcribe-and-save")
    public ResponseEntity<?> transcribe(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            // 이유: 프론트는 success/data 구조만 처리하므로, 인증 실패도 ApiResponse로 감싸 일관된 형식을 유지한다.
            return ResponseEntity.status(401)
                    .body(new ApiResponse<>(false, null, "로그인이 필요합니다."));
        }
        User user = authUser.getUser();

        try {
            byte[] bytes = file.getBytes();
            bytes = sttService.maybeReencodeToM4a(bytes, file.getOriginalFilename());

            String transcript = whisperService.transcribe(bytes, file.getOriginalFilename(), "ko");

            Transcription t = Transcription.builder()
                    .user(user)
                    .fileName(file.getOriginalFilename())
                    .resultText(transcript)
                    .summary(null)
                    .summaryType(null)
                    .createdAt(LocalDateTime.now())
                    .build();

            repo.save(t);

            // 이유: 프론트가 기대하는 success/data/message 구조로 응답해, 성공 시에도 오류 팝업이 뜨지 않도록 함.
            return ResponseEntity.ok(new ApiResponse<>(true, t, "STT 변환이 완료되었습니다."));

        } catch (Exception e) {
            // 이유: 실패 응답도 동일한 포맷을 사용해야 프론트에서 메시지를 파싱해 사용자에게 안내할 수 있다.
            return ResponseEntity.status(422)
                    .body(new ApiResponse<>(false, null, "STT 변환 실패: " + e.getMessage()));
        }
    }
}
