package com.example.record.review.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "review_questions")
public class ReviewQuestion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "custom_text", columnDefinition = "TEXT")
    private String customText;
}
