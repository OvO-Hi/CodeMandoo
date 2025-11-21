package com.example.record.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageService {

    /**
     * 실제 파일이 저장될 루트 디렉토리
     * 예) ./uploads/profile-images
     */
    private final String profileImageDir;

    /**
     * 클라이언트에 내려줄 때 사용할 URL prefix
     * 예) /uploads/profile-images
     */
    private final String profileImageUrlPrefix;

    public LocalFileStorageService(
            @Value("${app.upload.profile-image-dir:uploads/profile-images}") String profileImageDir,
            @Value("${app.upload.profile-image-url-prefix:/uploads/profile-images}") String profileImageUrlPrefix
    ) {
        this.profileImageDir = profileImageDir;
        this.profileImageUrlPrefix = profileImageUrlPrefix;
    }

    public String saveProfileImage(String userId, MultipartFile file) {
        try {
            // 디렉토리 생성
            Path uploadDir = Paths.get(profileImageDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            // 파일 확장자 추출
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 파일명: userId_타임스탬프_UUID.ext
            String filename = userId + "_" + Instant.now().toEpochMilli() + "_" + UUID.randomUUID() + ext;

            // 타겟 경로
            Path target = uploadDir.resolve(filename);
            file.transferTo(target.toFile());

            // 클라이언트에 내려줄 URL (예: /uploads/profile-images/xxx.png)
            String url = profileImageUrlPrefix + "/" + filename;

            log.info("Saved profile image for user {} at {}", userId, target);
            return url;
        } catch (Exception e) {
            log.error("Failed to save profile image for user {}", userId, e);
            throw new RuntimeException("프로필 이미지를 저장할 수 없습니다.", e);
        }
    }
}
