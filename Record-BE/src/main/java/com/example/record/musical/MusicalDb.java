package com.example.record.musical;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 뮤지컬 데이터베이스 엔티티
 * 
 * 이 엔티티는 왜 필요한가요?
 * 1. 뮤지컬 정보 관리: 뮤지컬의 기본 정보를 체계적으로 저장
 * 2. 프롬프트 생성 지원: AI 이미지 생성 시 뮤지컬 특성을 반영
 * 3. 사용자 경험 향상: 뮤지컬별 맞춤형 질문과 이미지 제공
 * 4. 데이터 분석: 어떤 뮤지컬이 인기 있는지 분석 가능
 * 
 * 사용 사례:
 * - "캣츠" 뮤지컬의 배경, 캐릭터 수, 요약 정보 저장
 * - 사용자가 "캣츠" 리뷰 작성 시 관련 정보를 프롬프트에 반영
 * - 뮤지컬별 특성에 맞는 이미지 생성
 */
@Entity
@Table(name = "musical_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicalDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 뮤지컬 제목
     * 
     * 예시: "캣츠", "레 미제라블", "오페라의 유령"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 식별: 뮤지컬을 구분하는 주요 기준
     * 2. 검색: 사용자가 특정 뮤지컬을 찾을 때 사용
     * 3. 매칭: 사용자 리뷰와 뮤지컬 정보를 연결
     */
    @Column(name = "title", length = 50, nullable = false)
    private String title;

    /**
     * 뮤지컬 요약
     * 
     * 뮤지컬의 줄거리나 주요 내용을 간단히 설명합니다.
     * 
     * 예시: "고양이들의 연례 축제에서 가장 특별한 고양이를 선택하는 이야기"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: AI 이미지 생성 시 스토리 요소 반영
     * 2. 사용자 이해: 뮤지컬에 대한 기본 정보 제공
     * 3. 맞춤형 질문: 뮤지컬 내용에 맞는 질문 생성
     */
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    /**
     * 뮤지컬 배경
     * 
     * 뮤지컬이 설정된 시대나 장소를 나타냅니다.
     * 
     * 예시: "19세기 파리", "현대 뉴욕", "판타지 세계"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 이미지 생성: 배경에 맞는 시각적 요소 생성
     * 2. 분류: 시대별, 장르별 뮤지컬 분류
     * 3. 추천: 사용자 선호도에 맞는 뮤지컬 추천
     */
    @Column(name = "background", length = 50)
    private String background;

    /**
     * 주요 캐릭터 수
     * 
     * 뮤지컬에 등장하는 주요 캐릭터의 개수입니다.
     * 
     * 예시: "캣츠"는 20여 명의 고양이 캐릭터가 등장
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 캐릭터 수에 맞는 이미지 생성
     * 2. 복잡도 파악: 뮤지컬의 복잡도 측정
     * 3. 질문 생성: 캐릭터 수에 맞는 질문 생성
     */
    @Column(name = "main_character_count")
    private Integer mainCharacterCount;

    /**
     * 뮤지컬 정보 생성 시간
     * 
     * @CreationTimestamp: 데이터베이스에 저장할 때 자동으로 현재 시간 설정
     * 이는 DB의 DEFAULT now()와 동일한 효과를 가집니다.
     * 
     * 사용 사례:
     * 1. 최신 뮤지컬 우선 표시
     * 2. 데이터 관리: 오래된 정보 정리
     * 3. 통계: 뮤지컬 정보 추가 패턴 분석
     * 
     * @Column: 데이터베이스의 created_at 컬럼과 매핑
     * 데이터베이스에서 DEFAULT now()로 설정되어 자동으로 현재 시간이 저장됩니다.
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    /**
     * 이 뮤지컬에 등장하는 캐릭터들
     * 
     * @OneToMany: 하나의 뮤지컬이 여러 캐릭터를 가질 수 있음을 의미
     * mappedBy: MusicalCharacter 엔티티의 musical 필드와 연결
     * cascade: 뮤지컬이 삭제되면 관련된 모든 캐릭터도 함께 삭제
     * orphanRemoval: 부모가 없는 캐릭터들을 자동으로 삭제
     * fetch: LAZY로 설정해서 필요할 때만 로드 (성능 최적화)
     * 
     * 사용 사례:
     * 1. 캐릭터 정보 조회: 뮤지컬의 모든 캐릭터 정보
     * 2. 프롬프트 생성: 캐릭터 정보를 이미지 생성에 반영
     * 3. 질문 생성: 캐릭터별 맞춤형 질문 생성
     */
    @OneToMany(mappedBy = "musical", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MusicalCharacter> characters = new ArrayList<>();


    /**
     * 캐릭터를 뮤지컬에 추가합니다.
     * 
     * 이 메서드는 왜 필요한가요?
     * 1. 양방향 관계 관리: 캐릭터가 뮤지컬을 참조하고, 뮤지컬도 캐릭터를 참조
     * 2. 데이터 일관성: 캐릭터 추가 시 자동으로 뮤지컬과 연결
     * 3. 편의성: 개발자가 매번 양방향 관계를 설정할 필요 없음
     * 
     * @param character 추가할 캐릭터
     */
    public void addCharacter(MusicalCharacter character) {
        character.setMusical(this);
        characters.add(character);
    }
}
