package com.example.record.musical;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 뮤지컬 캐릭터 레포지토리
 * 
 * 이 레포지토리는 뮤지컬 캐릭터 정보를 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 캐릭터 검색
 * 2. 뮤지컬별 캐릭터 조회
 * 3. 캐릭터 특성별 조회
 * 4. 프롬프트 생성 지원
 */
public interface MusicalCharacterRepository extends JpaRepository<MusicalCharacter, Long> {

    /**
     * 특정 뮤지컬의 모든 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - "캣츠" 뮤지컬의 모든 캐릭터 조회
     * - 뮤지컬 상세 정보 표시
     * 
     * @param musicalId 뮤지컬 ID
     * @return 해당 뮤지컬의 모든 캐릭터 목록
     */
    List<MusicalCharacter> findByMusicalId(Long musicalId);

    /**
     * 특정 뮤지컬의 특정 성별 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - "캣츠" 뮤지컬의 여성 캐릭터들 조회
     * - 성별별 캐릭터 분석
     * 
     * @param musicalId 뮤지컬 ID
     * @param gender 캐릭터 성별
     * @return 해당 뮤지컬의 특정 성별 캐릭터 목록
     */
    List<MusicalCharacter> findByMusicalIdAndGender(Long musicalId, String gender);

    /**
     * 특정 뮤지컬의 특정 나이대 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - "캣츠" 뮤지컬의 젊은 캐릭터들 조회
     * - 나이대별 캐릭터 분석
     * 
     * @param musicalId 뮤지컬 ID
     * @param age 캐릭터 나이대
     * @return 해당 뮤지컬의 특정 나이대 캐릭터 목록
     */
    List<MusicalCharacter> findByMusicalIdAndAge(Long musicalId, String age);

    /**
     * 특정 뮤지컬의 특정 직업/역할 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - "캣츠" 뮤지컬의 댄서 캐릭터들 조회
     * - 역할별 캐릭터 분석
     * 
     * @param musicalId 뮤지컬 ID
     * @param occupation 캐릭터 직업/역할
     * @return 해당 뮤지컬의 특정 직업/역할 캐릭터 목록
     */
    List<MusicalCharacter> findByMusicalIdAndOccupation(Long musicalId, String occupation);

    /**
     * 이름으로 캐릭터를 검색합니다.
     * 
     * 사용 예시:
     * - "그리자벨라" 캐릭터 검색
     * - 캐릭터 정보 자동 완성
     * 
     * @param name 캐릭터 이름
     * @return 해당 이름의 캐릭터 목록
     */
    List<MusicalCharacter> findByName(String name);

    /**
     * 이름에 특정 키워드가 포함된 캐릭터들을 검색합니다.
     * 
     * 사용 예시:
     * - "그리"를 검색하면 "그리자벨라"가 검색됨
     * - 부분 검색 지원
     * 
     * @param keyword 검색 키워드
     * @return 키워드가 포함된 캐릭터 목록
     */
    @Query("SELECT c FROM MusicalCharacter c WHERE c.name LIKE %:keyword%")
    List<MusicalCharacter> findByNameContaining(@Param("keyword") String keyword);

    /**
     * 특정 성별의 모든 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - 모든 여성 캐릭터 조회
     * - 성별별 캐릭터 통계
     * 
     * @param gender 캐릭터 성별
     * @return 해당 성별의 모든 캐릭터 목록
     */
    List<MusicalCharacter> findByGender(String gender);

    /**
     * 특정 직업/역할의 모든 캐릭터를 조회합니다.
     * 
     * 사용 예시:
     * - 모든 댄서 캐릭터 조회
     * - 역할별 캐릭터 통계
     * 
     * @param occupation 캐릭터 직업/역할
     * @return 해당 직업/역할의 모든 캐릭터 목록
     */
    List<MusicalCharacter> findByOccupation(String occupation);
}

