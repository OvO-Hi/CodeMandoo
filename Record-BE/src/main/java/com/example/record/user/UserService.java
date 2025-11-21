package com.example.record.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final LocalFileStorageService localFileStorageService;

    @Transactional
    public User updateProfile(User user, UserController.UpdateProfileRequest req) {

        if (req.getNickname() != null) {
            user.setNickname(req.getNickname());
        }
        if (req.getEmail() != null) {
            user.setEmail(req.getEmail());
        }
        if (req.getIsAccountPrivate() != null) {
            user.setIsAccountPrivate(req.getIsAccountPrivate());
        }

        return userRepository.save(user);
    }

    // ────────────────────────────────────────────
    //  프로필 이미지 변경 (기존 이미지 삭제 + 새 이미지 저장)
    // ────────────────────────────────────────────
    @Transactional
    public User updateProfileImage(User user, MultipartFile file) {
        validateImage(file);

        // 1) 기존 이미지 삭제
        deleteOldImage(user.getProfileImage());

        // 2) 새 이미지 저장
        String imageUrl = localFileStorageService.saveProfileImage(user.getId(), file);

        // 3) User 엔티티에 새로운 URL 반영
        user.setProfileImage(imageUrl);

        // 4) DB 저장
        return userRepository.save(user);
    }

    // ────────────────────────────────────────────
    //  기존 프로필 이미지 파일 삭제
    // ────────────────────────────────────────────
    private void deleteOldImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        try {
            // URL → 파일 이름만 추출
            String filename = imageUrl.replace("/uploads/profile-images/", "");

            // 실제 서버의 파일 경로로 변환
            Path filePath = Paths.get("uploads/profile-images").resolve(filename);

            // 파일 삭제
            Files.deleteIfExists(filePath);

        } catch (Exception e) {
            // 삭제 실패해도 기능 자체는 계속 진행
            System.out.println("⚠ 기존 프로필 이미지 삭제 실패: " + e.getMessage());
        }
    }

    // ────────────────────────────────────────────
    //  이미지 유효성 검사
    // ────────────────────────────────────────────
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어 있습니다.");
        }

        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("이미지 용량은 5MB 이하여야 합니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드할 수 있습니다.");
        }
    }
}
