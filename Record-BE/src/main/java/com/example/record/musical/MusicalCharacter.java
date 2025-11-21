package com.example.record.musical;

import jakarta.persistence.*;
import lombok.*;

/**
 * 뮤지컬 캐릭터 엔티티
 * 
 * 이 엔티티는 왜 필요한가요?
 * 1. 캐릭터 정보 관리: 뮤지컬의 각 캐릭터에 대한 상세 정보 저장
 * 2. 프롬프트 생성 지원: AI 이미지 생성 시 캐릭터 특성을 반영
 * 3. 사용자 경험 향상: 캐릭터별 맞춤형 질문과 이미지 제공
 * 4. 데이터 분석: 어떤 캐릭터가 인기 있는지 분석 가능
 * 
 * 사용 사례:
 * - "캣츠"의 "그리자벨라" 캐릭터 정보 저장
 * - 사용자가 "그리자벨라"에 대해 리뷰 작성 시 관련 정보를 프롬프트에 반영
 * - 캐릭터별 특성에 맞는 이미지 생성
 */
@Entity
@Table(name = "musical_characters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MusicalCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이 캐릭터가 속한 뮤지컬
     * 
     * @ManyToOne: 여러 캐릭터가 하나의 뮤지컬에 속할 수 있음을 의미
     * FetchType.LAZY: 뮤지컬 정보가 필요할 때만 데이터베이스에서 가져옴 (성능 최적화)
     * 
     * 예시: "그리자벨라" 캐릭터는 "캣츠" 뮤지컬에 속함
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "musical_id", referencedColumnName = "id")
    private MusicalDb musical;

    /**
     * 캐릭터 이름
     * 
     * 예시: "그리자벨라", "맥베티", "미스터 미스토펠리스"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 식별: 캐릭터를 구분하는 주요 기준
     * 2. 검색: 사용자가 특정 캐릭터를 찾을 때 사용
     * 3. 매칭: 사용자 리뷰와 캐릭터 정보를 연결
     */
    @Column(name = "name", length = 50)
    private String name;

    /**
     * 캐릭터 성별
     * 
     * 예시: "MALE", "FEMALE", "UNKNOWN"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 성별에 맞는 이미지 생성
     * 2. 분류: 성별별 캐릭터 분류
     * 3. 질문 생성: 성별에 맞는 질문 생성
     */
    @Column(name = "gender", length = 10)
    private String gender;

    /**
     * 캐릭터 나이
     * 
     * 예시: "YOUNG", "MIDDLE", "OLD", "UNKNOWN"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 나이에 맞는 이미지 생성
     * 2. 분류: 나이별 캐릭터 분류
     * 3. 질문 생성: 나이에 맞는 질문 생성
     */
    @Column(name = "age", length = 10)
    private String age;

    /**
     * 캐릭터 직업/역할
     * 
     * 예시: "DANCER", "SINGER", "LEADER", "VILLAIN"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 직업/역할에 맞는 이미지 생성
     * 2. 분류: 역할별 캐릭터 분류
     * 3. 질문 생성: 역할에 맞는 질문 생성
     */
    @Column(name = "occupation", length = 50)
    private String occupation;

    /**
     * 캐릭터 설명
     * 
     * 캐릭터의 성격, 특징, 역할 등을 자세히 설명합니다.
     * 
     * 예시: "과거의 영광을 잃고 쓸쓸해하는 고양이. 'Memory' 노래로 유명"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 캐릭터 특성을 이미지 생성에 반영
     * 2. 사용자 이해: 캐릭터에 대한 상세 정보 제공
     * 3. 맞춤형 질문: 캐릭터 특성에 맞는 질문 생성
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}

