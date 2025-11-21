package com.example.record.review.repository;

import com.example.record.review.entity.ReviewQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewQuestionRepository extends JpaRepository<ReviewQuestion, Long> {
}