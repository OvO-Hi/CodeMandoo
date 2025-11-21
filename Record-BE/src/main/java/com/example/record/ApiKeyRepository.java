// ApiKeyRepository: ApiKey 엔티티에 대한 DB 접근을 제공하는 JPA 리포지토리 인터페이스입니다.

package com.example.record;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {

    // 특정 API 키가 DB에 존재하는지 여부를 확인
    boolean existsByApiKey(String apiKey);
}
