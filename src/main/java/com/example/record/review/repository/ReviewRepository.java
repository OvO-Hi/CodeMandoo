// com.example.record.review.repository.ReviewRepository
package com.example.record.review.repository;

import com.example.record.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Ticket.userId(String)에 매핑된 중첩 프로퍼티 경로 사용
    Page<Review> findByTicket_UserId(String userId, Pageable pageable);
}
