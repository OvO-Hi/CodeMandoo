package com.example.record.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(length = 15)
    private String id;

    @Column(length = 30, nullable = false, unique = true)
    private String email;

    @Column(length = 300, nullable = false)
    private String password;

    @Column(length = 30, nullable = false)
    private String nickname;

    @Column(length = 10, nullable = false)
    @Builder.Default
    private String role = "USER";

    @Column(columnDefinition = "TEXT")
    private String favorite;

    // ⭐ 실제 DB에 저장되는 필드
    @Column(name = "is_account_private", nullable = false)
    @Builder.Default
    private Boolean isAccountPrivate = false;

    @Column(name = "profile_image", length = 500)
    private String profileImage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
