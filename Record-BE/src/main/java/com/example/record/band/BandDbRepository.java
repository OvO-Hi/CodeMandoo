package com.example.record.band;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 밴드 데이터베이스 레포지토리
 * 
 * 이 레포지토리는 밴드 정보를 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 밴드 검색
 * 2. 밴드 정보 조회
 * 3. 장르별 밴드 조회
 * 4. 프롬프트 생성 지원
 */
public interface BandDbRepository extends JpaRepository<BandDb, Long> {

    /**
     * 밴드 이름으로 밴드를 검색합니다.
     * 
     * 사용 예시:
     * - 사용자가 "콜드플레이"를 검색할 때
     * - 밴드 정보 자동 완성
     * 
     * @param bandName 밴드 이름
     * @return 해당 이름의 밴드 (없으면 Optional.empty())
     */
    Optional<BandDb> findByBandName(String bandName);

    /**
     * 밴드 이름에 특정 키워드가 포함된 밴드들을 검색합니다.
     * 
     * 사용 예시:
     * - "콜드"를 검색하면 "콜드플레이"가 검색됨
     * - 부분 검색 지원
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 밴드 목록
     */
    @Query("SELECT b FROM BandDb b WHERE b.bandName LIKE %:keyword%")
    List<BandDb> findByBandNameContaining(@Param("keyword") String keyword);

    /**
     * 장르로 밴드를 검색합니다.
     * 
     * 사용 예시:
     * - "ROCK" 장르의 모든 밴드 조회
     * - 장르별 밴드 분류
     * 
     * @param genre 밴드 장르
     * @return 해당 장르의 밴드 목록
     */
    List<BandDb> findByGenre(String genre);

    /**
     * 포스터 색상으로 밴드를 검색합니다.
     * 
     * 사용 예시:
     * - 파란색을 주로 사용하는 밴드들 조회
     * - 색상별 밴드 분류
     * 
     * @param posterColor 포스터 색상
     * @return 해당 색상을 사용하는 밴드 목록
     */
    List<BandDb> findByPosterColor(String posterColor);

    /**
     * 밴드 상징으로 밴드를 검색합니다.
     * 
     * 사용 예시:
     * - "V" 모양 로고를 사용하는 밴드들 조회
     * - 상징별 밴드 분류
     * 
     * @param bandSymbol 밴드 상징
     * @return 해당 상징을 사용하는 밴드 목록
     */
    List<BandDb> findByBandSymbol(String bandSymbol);

    /**
     * 장르와 색상을 모두 고려한 밴드를 검색합니다.
     * 
     * 사용 예시:
     * - "ROCK" 장르이면서 파란색을 사용하는 밴드들 조회
     * - 복합 조건 검색
     * 
     * @param genre 밴드 장르
     * @param posterColor 포스터 색상
     * @return 조건에 맞는 밴드 목록
     */
    List<BandDb> findByGenreAndPosterColor(String genre, String posterColor);

    /**
     * 밴드 이름으로 밴드 정보를 조회합니다 (대소문자 구분 없음).
     * 
     * 사용 예시:
     * - "coldplay", "Coldplay", "COLDPLAY" 모두 검색 가능
     * - 사용자 입력 오타 방지
     * 
     * @param bandName 밴드 이름
     * @return 해당 이름의 밴드 (없으면 Optional.empty())
     */
    @Query("SELECT b FROM BandDb b WHERE LOWER(b.bandName) = LOWER(:bandName)")
    Optional<BandDb> findByBandNameIgnoreCase(@Param("bandName") String bandName);

    /**
     * 밴드 이름에 특정 키워드가 포함된 밴드들을 검색합니다 (대소문자 구분 없음).
     * 
     * 사용 예시:
     * - "cold"를 검색하면 "Coldplay"가 검색됨
     * - 대소문자 구분 없는 부분 검색
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 밴드 목록
     */
    @Query("SELECT b FROM BandDb b WHERE LOWER(b.bandName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BandDb> findByBandNameContainingIgnoreCase(@Param("keyword") String keyword);
}

