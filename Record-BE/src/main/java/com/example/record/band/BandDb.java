package com.example.record.band;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * 밴드 데이터베이스 엔티티
 * 
 * 이 엔티티는 왜 필요한가요?
 * 1. 밴드 정보 관리: 밴드의 기본 정보를 체계적으로 저장
 * 2. 프롬프트 생성 지원: AI 이미지 생성 시 밴드 특성을 반영
 * 3. 사용자 경험 향상: 밴드별 맞춤형 질문과 이미지 제공
 * 4. 데이터 분석: 어떤 밴드가 인기 있는지 분석 가능
 * 
 * 사용 사례:
 * - "콜드플레이" 밴드의 이름, 의미, 상징, 색상 정보 저장
 * - 사용자가 "콜드플레이" 리뷰 작성 시 관련 정보를 프롬프트에 반영
 * - 밴드별 특성에 맞는 이미지 생성
 */
@Entity
@Table(name = "band_db")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BandDb {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 밴드 이름
     * 
     * 예시: "콜드플레이", "라디오헤드", "아델"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 식별: 밴드를 구분하는 주요 기준
     * 2. 검색: 사용자가 특정 밴드를 찾을 때 사용
     * 3. 매칭: 사용자 리뷰와 밴드 정보를 연결
     */
    @Column(name = "band_name", length = 50, nullable = false)
    private String bandName;

    /**
     * 밴드 이름의 의미
     * 
     * 밴드 이름이 가지는 특별한 의미나 유래를 설명합니다.
     * 
     * 예시: "콜드플레이"는 "차가운 놀이"라는 의미로, 차분하고 감성적인 음악을 표현
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 밴드 이름의 의미를 이미지 생성에 반영
     * 2. 사용자 이해: 밴드에 대한 깊이 있는 정보 제공
     * 3. 맞춤형 질문: 밴드 이름 의미에 맞는 질문 생성
     */
    @Column(name = "band_name_meaning", columnDefinition = "TEXT")
    private String bandNameMeaning;

    /**
     * 밴드 상징
     * 
     * 밴드를 대표하는 상징이나 로고를 설명합니다.
     * 
     * 예시: "콜드플레이"는 "V" 모양의 로고로 유명
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 밴드 상징을 이미지 생성에 반영
     * 2. 시각적 요소: 밴드의 시각적 정체성 표현
     * 3. 브랜딩: 밴드의 브랜드 이미지 강화
     */
    @Column(name = "band_symbol", length = 255)
    private String bandSymbol;

    /**
     * 포스터 색상
     * 
     * 밴드의 포스터나 앨범 커버에서 주로 사용하는 색상을 나타냅니다.
     * 
     * 예시: "콜드플레이"는 파란색과 흰색을 주로 사용
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 프롬프트 생성: 밴드 색상을 이미지 생성에 반영
     * 2. 시각적 일관성: 밴드의 시각적 정체성 유지
     * 3. 감정 표현: 색상을 통한 감정 전달
     */
    @Column(name = "poster_color", length = 50)
    private String posterColor;

    /**
     * 밴드 장르
     * 
     * 밴드의 음악 장르를 나타냅니다.
     * 
     * 예시: "ROCK", "POP", "INDIE", "ELECTRONIC"
     * 
     * 왜 이 필드가 중요한가요?
     * 1. 분류: 장르별 밴드 분류
     * 2. 프롬프트 생성: 장르에 맞는 이미지 생성
     * 3. 추천: 사용자 선호도에 맞는 밴드 추천
     */
    @Column(name = "genre", length = 100)
    private String genre;

    /**
     * 밴드 정보 생성 시간
     * 
     * @CreationTimestamp: 데이터베이스에 저장할 때 자동으로 현재 시간 설정
     * 이는 DB의 DEFAULT now()와 동일한 효과를 가집니다.
     * 
     * 사용 사례:
     * 1. 최신 밴드 우선 표시
     * 2. 데이터 관리: 오래된 정보 정리
     * 3. 통계: 밴드 정보 추가 패턴 분석
     * 
     * @Column: 데이터베이스의 created_at 컬럼과 매핑
     * 데이터베이스에서 DEFAULT now()로 설정되어 자동으로 현재 시간이 저장됩니다.
     */
    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
