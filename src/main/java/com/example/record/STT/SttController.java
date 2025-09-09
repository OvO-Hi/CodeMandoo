package com.example.record.STT;

import com.example.record.DB.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stt")
public class SttController {

    private final SttService sttService; // 음성 → 텍스트
    private final SttGptService sttGptService; // 요약
    private final TranscriptionRepository transcriptionRepository;

    // [1] 음성 파일 업로드 → 텍스트 변환 → DB 저장
    @PostMapping
    public ResponseEntity<String> transcribe(@RequestParam MultipartFile file,
                                             @AuthenticationPrincipal User user) throws Exception {
        if (user == null) return ResponseEntity.status(401).body("Unauthorized");

        // 확장자 추출 (.wav/.mp3… 없으면 .tmp)
        String suffix = ".tmp";
        String original = file.getOriginalFilename();
        if (original != null && original.lastIndexOf('.') != -1) {
            suffix = original.substring(original.lastIndexOf('.')); // 포함된 점부터
        }

        // OS 독립적인 안전 임시 파일
        Path temp = Files.createTempFile("stt_", suffix);
        try {
            file.transferTo(temp.toFile());

            String result = sttService.transcribeLocalFile(temp.toString());

            Transcription t = Transcription.builder()
                    .fileName(temp.getFileName().toString())
                    .resultText(result)
                    .createdAt(LocalDateTime.now())
                    .user(user)
                    .build();
            transcriptionRepository.save(t);

            return ResponseEntity.ok(result);
        } finally {
            try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
        }
    }

    // [2] 요약 생성 (id로 본인 기록만 허용)
    @PostMapping("/gpt")
    public ResponseEntity<GptResponse> summarize(@RequestParam Long id,
                                                 @AuthenticationPrincipal User user) {
        // 401: 제네릭을 GptResponse로 유지
        if (user == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body((GptResponse) null);
            // 또는 .<GptResponse>body(null)
        }

        return transcriptionRepository.findById(id)
                .map(t -> {
                    // 403: 여기서도 제네릭 유지
                    if (!t.getUser().getId().equals(user.getId())) {
                        return ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body((GptResponse) null);
                    }
                    String summary = sttGptService.summarize(t.getResultText());
                    t.setSummary(summary);
                    transcriptionRepository.save(t);
                    return ResponseEntity.ok(new GptResponse(summary));
                })
                // 404: 이미 제네릭 명시되어 OK
                .orElseGet(() -> ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .<GptResponse>body(null));
    }


    // [3] 내 기록 조회
    @GetMapping("/list")
    public ResponseEntity<List<TranscriptionResponse>> list(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();

        List<Transcription> transcriptions = transcriptionRepository.findByUser(user);
        List<TranscriptionResponse> response = transcriptions.stream()
                .map(t -> new TranscriptionResponse(
                        t.getId(),
                        t.getFileName(),
                        t.getResultText(),
                        t.getCreatedAt(),
                        t.getSummary()
                )).toList();
        return ResponseEntity.ok(response);
    }
}
