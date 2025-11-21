package com.example.record.STTorText.entity;

import com.example.record.STTorText.review.ReviewType;
import com.example.record.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transcription {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    /**
     * STT 변환 결과 텍스트
     * 
     * @Lob 대신 @Column(columnDefinition = "TEXT")를 사용하는 이유:
     * - PostgreSQL에서 @Lob는 oid 타입으로 매핑되는데, 기존 DB 스키마가 TEXT 타입이면
     *   Hibernate가 자동으로 컬럼 타입을 변경하려고 시도하면서 오류가 발생합니다.
     * - TEXT 타입으로 명시적으로 지정하면 기존 스키마와 호환되며,
     *   대용량 텍스트도 충분히 저장할 수 있습니다.
     */
    @Column(columnDefinition = "TEXT")
    private String resultText;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Enumerated(EnumType.STRING)
    private ReviewType summaryType;
}
