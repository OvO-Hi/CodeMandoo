package com.example.record.review.repository;

import com.example.record.review.entity.UserCustomQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 사용자 맞춤 질문 레포지토리
 * 
 * 역할: 사용자 맞춤 질문을 데이터베이스에서 조회하고 관리
 */
public interface UserCustomQuestionRepository extends JpaRepository<UserCustomQuestion, Long> {

    /**
     * 특정 사용자의 맞춤 질문들을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @return 해당 사용자의 맞춤 질문 목록
     */
    List<UserCustomQuestion> findByUser_Id(String userId);

    /**
     * 특정 사용자와 장르의 맞춤 질문들을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param genre 장르 (예: "MUSICAL", "BAND")
     * @return 조건에 맞는 맞춤 질문 목록
     */
    List<UserCustomQuestion> findByUser_IdAndGenre(String userId, String genre);

    /**
     * 특정 사용자와 장르의 랜덤 맞춤 질문을 조회합니다.
     * 
     * @param userId 사용자 ID
     * @param genre 장르
     * @param limit 조회할 질문 개수
     * @return 랜덤하게 선택된 맞춤 질문 목록
     */
    @Query(value = "SELECT * FROM user_custom_questions WHERE user_id = :userId AND genre = :genre ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<UserCustomQuestion> findRandomByUserAndGenre(@Param("userId") String userId, @Param("genre") String genre, @Param("limit") int limit);
}

