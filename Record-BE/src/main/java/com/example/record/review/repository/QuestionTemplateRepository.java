package com.example.record.review.repository;

import com.example.record.review.entity.QuestionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 질문 템플릿 레포지토리
 * 
 * 이 레포지토리는 질문 템플릿을 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 카테고리별 질문 조회
 * 2. 장르별 질문 조회  
 * 3. 랜덤 질문 선택
 * 4. 사용자 맞춤형 질문 생성 (향후 구현)
 */
public interface QuestionTemplateRepository extends JpaRepository<QuestionTemplate, Long> {

    /**
     * 특정 카테고리의 질문 템플릿을 조회합니다.
     * 
     * 사용 예시:
     * - "PERFORMANCE" 카테고리의 모든 질문 조회
     * - "MUSIC" 카테고리의 모든 질문 조회
     * 
     * @param category 질문 카테고리 (예: "PERFORMANCE", "MUSIC", "STAGE")
     * @return 해당 카테고리의 질문 템플릿 목록
     */
    List<QuestionTemplate> findByCategory(String category);

    /**
     * 특정 장르의 질문 템플릿을 조회합니다.
     * 
     * 사용 예시:
     * - "musical" 또는 "MUSICAL" 장르의 모든 질문 조회 (대소문자 구분 없음)
     * - "band" 또는 "BAND" 장르의 모든 질문 조회
     * - "common" 또는 "COMMON" 장르의 모든 질문 조회 (모든 장르에 공통)
     * 
     * 변경 사항:
     * - 대소문자 구분 없이 조회하도록 수정 (LOWER() 함수 사용)
     * - 이유: DB에 저장된 장르가 대문자일 수도 있고 소문자일 수도 있어서
     * 
     * @param genre 장르 (예: "musical", "MUSICAL", "band", "BAND", "common", "COMMON")
     * @return 해당 장르의 질문 템플릿 목록
     */
    @Query(value = "SELECT * FROM questions_templates WHERE LOWER(genre) = LOWER(:genre)", nativeQuery = true)
    List<QuestionTemplate> findByGenre(@Param("genre") String genre);

    /**
     * 카테고리와 장르를 모두 고려한 질문 템플릿을 조회합니다.
     * 
     * 사용 예시:
     * - 뮤지컬 공연의 연기 관련 질문들 조회
     * - 밴드 공연의 음악 관련 질문들 조회
     * 
     * @param category 질문 카테고리
     * @param genre 장르
     * @return 조건에 맞는 질문 템플릿 목록
     */
    List<QuestionTemplate> findByCategoryAndGenre(String category, String genre);

    /**
     * 특정 장르의 랜덤 질문을 조회합니다.
     * 
     * 이 메서드는 사용자가 리뷰를 작성할 때 랜덤하게 질문을 제공하는 데 사용됩니다.
     * 
     * 사용 예시:
     * - 뮤지컬 리뷰 작성 시 랜덤 질문 3개 제공
     * - 밴드 리뷰 작성 시 랜덤 질문 3개 제공
     * 
     * 변경 사항:
     * - 대소문자 구분 없이 조회하도록 수정 (LOWER() 함수 사용)
     * - 이유: DB에 저장된 장르가 대문자일 수도 있고 소문자일 수도 있어서
     * 
     * @param genre 장르 (대소문자 구분 없음)
     * @param limit 조회할 질문 개수
     * @return 랜덤하게 선택된 질문 템플릿 목록
     */
    @Query(value = "SELECT * FROM questions_templates WHERE LOWER(genre) = LOWER(:genre) ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuestionTemplate> findRandomByGenre(@Param("genre") String genre, @Param("limit") int limit);

    /**
     * 특정 카테고리와 장르의 랜덤 질문을 조회합니다.
     * 
     * 이 메서드는 더 세밀한 질문 선택을 위해 사용됩니다.
     * 
     * @param category 질문 카테고리
     * @param genre 장르
     * @param limit 조회할 질문 개수
     * @return 랜덤하게 선택된 질문 템플릿 목록
     */
    @Query(value = "SELECT * FROM questions_templates WHERE category = :category AND genre = :genre ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<QuestionTemplate> findRandomByCategoryAndGenre(@Param("category") String category, @Param("genre") String genre, @Param("limit") int limit);
}

