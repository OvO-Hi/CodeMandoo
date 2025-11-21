// S3Controller: 클라이언트로부터 업로드된 파일을 받아 AWS S3에 저장하고, 업로드된 파일의 URL을 반환하는 컨트롤러입니다.


package com.example.record.AWS;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;

    // POST /upload 요청을 받아 파일을 S3에 업로드하고 업로드된 파일의 URL을 반환
    @PostMapping("/upload")
    public ResponseEntity<String> upload(@RequestParam("file") MultipartFile file) {
        try {
            // S3에 파일 업로드 수행
            String uploadedUrl = s3Service.uploadFile(file);

            // 업로드된 파일의 URL 반환
            return ResponseEntity.ok(uploadedUrl);
        } catch (IOException e) {
            // 업로드 실패 시 500 오류 응답
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 중 오류 발생: " + e.getMessage());
        }
    }
}

