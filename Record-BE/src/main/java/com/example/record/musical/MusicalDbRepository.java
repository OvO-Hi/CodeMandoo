package com.example.record.musical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 뮤지컬 데이터베이스 레포지토리
 * 
 * 이 레포지토리는 뮤지컬 정보를 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 뮤지컬 검색
 * 2. 뮤지컬 정보 조회
 * 3. 장르별 뮤지컬 조회
 * 4. 프롬프트 생성 지원
 */
public interface MusicalDbRepository extends JpaRepository<MusicalDb, Long> {

    /**
     * 제목으로 뮤지컬을 검색합니다.
     * 
     * 사용 예시:
     * - 사용자가 "캣츠"를 검색할 때
     * - 뮤지컬 정보 자동 완성
     * 
     * @param title 뮤지컬 제목
     * @return 해당 제목의 뮤지컬 (없으면 Optional.empty())
     */
    Optional<MusicalDb> findByTitle(String title);

    /**
     * 제목에 특정 키워드가 포함된 뮤지컬들을 검색합니다.
     * 
     * 사용 예시:
     * - "캣"을 검색하면 "캣츠"가 검색됨
     * - 부분 검색 지원
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 뮤지컬 목록
     */
    @Query("SELECT m FROM MusicalDb m WHERE m.title LIKE %:keyword%")
    List<MusicalDb> findByTitleContaining(@Param("keyword") String keyword);

    /**
     * 배경으로 뮤지컬을 검색합니다.
     * 
     * 사용 예시:
     * - "19세기 파리" 배경의 뮤지컬들 조회
     * - 시대별 뮤지컬 분류
     * 
     * @param background 뮤지컬 배경
     * @return 해당 배경의 뮤지컬 목록
     */
    List<MusicalDb> findByBackground(String background);

    /**
     * 캐릭터 수로 뮤지컬을 검색합니다.
     * 
     * 사용 예시:
     * - 캐릭터가 많은 뮤지컬들 조회
     * - 복잡도별 뮤지컬 분류
     * 
     * @param characterCount 캐릭터 수
     * @return 해당 캐릭터 수의 뮤지컬 목록
     */
    List<MusicalDb> findByMainCharacterCount(Integer characterCount);

    /**
     * 캐릭터 수가 특정 값 이상인 뮤지컬들을 검색합니다.
     * 
     * 사용 예시:
     * - 캐릭터가 10명 이상인 뮤지컬들 조회
     * - 복잡한 뮤지컬 필터링
     * 
     * @param minCharacterCount 최소 캐릭터 수
     * @return 캐릭터 수가 최소값 이상인 뮤지컬 목록
     */
    List<MusicalDb> findByMainCharacterCountGreaterThanEqual(Integer minCharacterCount);

    /**
     * 뮤지컬과 관련된 캐릭터 정보를 함께 조회합니다.
     * 
     * 사용 예시:
     * - 프롬프트 생성 시 뮤지컬과 캐릭터 정보를 함께 사용
     * - 상세 정보 조회
     * 
     * @param musicalId 뮤지컬 ID
     * @return 뮤지컬과 캐릭터 정보
     */
    @Query("SELECT m FROM MusicalDb m LEFT JOIN FETCH m.characters WHERE m.id = :musicalId")
    Optional<MusicalDb> findByIdWithCharacters(@Param("musicalId") Long musicalId);

    /**
     * 제목으로 뮤지컬과 캐릭터 정보를 함께 조회합니다.
     * 
     * 사용 예시:
     * - "캣츠" 뮤지컬과 모든 캐릭터 정보를 함께 조회
     * - 프롬프트 생성 시 사용
     * 
     * @param title 뮤지컬 제목
     * @return 뮤지컬과 캐릭터 정보
     */
    @Query("SELECT m FROM MusicalDb m LEFT JOIN FETCH m.characters WHERE m.title = :title")
    Optional<MusicalDb> findByTitleWithCharacters(@Param("title") String title);
}

