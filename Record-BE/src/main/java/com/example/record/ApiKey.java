// ApiKey: 사용자별 OpenAI API 키를 저장하는 엔티티 클래스입니다.

package com.example.record;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 ID
    private Long id;

    private String userId;  // API 키를 등록한 사용자 ID
    private String apiKey;  // 실제로 사용할 OpenAI API 키

    private LocalDateTime createdAt = LocalDateTime.now(); // 등록 시각

    public ApiKey() {}

    // 생성자: 사용자 ID와 API 키를 받아 초기화
    public ApiKey(String userId, String apiKey) {
        this.userId = userId;
        this.apiKey = apiKey;
    }

    // getter/setter 메서드
    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
