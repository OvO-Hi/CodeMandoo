package com.example.record.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    boolean existsByEmail(String email);

    // 아이디 찾기 (이메일로 조회)
    Optional<User> findByEmail(String email);

    // 비밀번호 재설정 (id + email 일치 검증)
    Optional<User> findByIdAndEmail(String id, String email);
}
