package com.example.record.review.repository;

import com.example.record.review.entity.GeneratedImageUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 생성된 이미지 URL 레포지토리
 * 
 * 이 레포지토리는 생성된 이미지들을 데이터베이스에서 조회하고 관리하는 역할을 합니다.
 * 
 * 주요 기능:
 * 1. 리뷰별 이미지 조회
 * 2. 선택된 이미지 조회
 * 3. 이미지 선택 상태 변경
 * 4. 이미지 히스토리 관리
 */
public interface GeneratedImageUrlRepository extends JpaRepository<GeneratedImageUrl, Long> {

    /**
     * 특정 리뷰의 모든 생성된 이미지를 조회합니다.
     * 
     * 사용 예시:
     * - 리뷰 상세 페이지에서 모든 생성된 이미지 표시
     * - 사용자가 이미지 선택할 때 옵션 제공
     * 
     * @param reviewId 리뷰 ID
     * @return 해당 리뷰의 모든 생성된 이미지 목록
     */
    List<GeneratedImageUrl> findByReviewId(Long reviewId);

    /**
     * 특정 리뷰의 선택된 이미지를 조회합니다.
     * 
     * 사용 예시:
     * - 리뷰 목록에서 대표 이미지 표시
     * - 친구에게 공유할 때 선택된 이미지 사용
     * 
     * @param reviewId 리뷰 ID
     * @return 선택된 이미지 (없으면 Optional.empty())
     */
    Optional<GeneratedImageUrl> findByReviewIdAndIsSelectedTrue(Long reviewId);

    /**
     * 특정 리뷰의 최신 생성된 이미지를 조회합니다.
     * 
     * 사용 예시:
     * - 사용자가 방금 생성한 이미지를 바로 표시
     * - 이미지 생성 후 미리보기 제공
     * 
     * @param reviewId 리뷰 ID
     * @return 최신 생성된 이미지 (없으면 Optional.empty())
     */
    @Query("SELECT gi FROM GeneratedImageUrl gi WHERE gi.review.id = :reviewId ORDER BY gi.createdAt DESC LIMIT 1")
    Optional<GeneratedImageUrl> findLatestByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 특정 리뷰의 특정 스타일 이미지를 조회합니다.
     * 
     * 사용 예시:
     * - "로맨틱" 스타일 이미지만 조회
     * - "액션" 스타일 이미지만 조회
     * 
     * @param reviewId 리뷰 ID
     * @param style 이미지 스타일
     * @return 해당 스타일의 이미지 목록
     */
    List<GeneratedImageUrl> findByReviewIdAndStyle(Long reviewId, String style);

    /**
     * 특정 리뷰의 모든 이미지 선택을 해제합니다.
     * 
     * 사용 예시:
     * - 사용자가 새로운 이미지를 선택할 때 기존 선택 해제
     * - 이미지 선택 상태 초기화
     * 
     * @param reviewId 리뷰 ID
     */
    @Modifying
    @Query("UPDATE GeneratedImageUrl gi SET gi.isSelected = false WHERE gi.review.id = :reviewId")
    void unselectAllByReviewId(@Param("reviewId") Long reviewId);

    /**
     * 특정 이미지를 선택 상태로 변경합니다.
     * 
     * 사용 예시:
     * - 사용자가 이미지를 선택했을 때 호출
     * - 다른 이미지들의 선택을 해제하고 이 이미지만 선택
     * 
     * @param imageId 이미지 ID
     */
    @Modifying
    @Query("UPDATE GeneratedImageUrl gi SET gi.isSelected = true WHERE gi.id = :imageId")
    void selectImage(@Param("imageId") Long imageId);

    /**
     * 특정 리뷰의 이미지 개수를 조회합니다.
     * 
     * 사용 예시:
     * - 사용자가 몇 개의 이미지를 생성했는지 표시
     * - 이미지 생성 제한 확인
     * 
     * @param reviewId 리뷰 ID
     * @return 이미지 개수
     */
    long countByReviewId(Long reviewId);

    /**
     * 특정 스타일의 이미지 개수를 조회합니다.
     * 
     * 사용 예시:
     * - 어떤 스타일이 가장 인기 있는지 분석
     * - 사용자 선호도 분석
     * 
     * @param style 이미지 스타일
     * @return 해당 스타일의 이미지 개수
     */
    long countByStyle(String style);
}

