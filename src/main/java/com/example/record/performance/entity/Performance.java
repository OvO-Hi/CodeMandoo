package com.example.record.performance.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "performances")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Performance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 필요한 만큼만 먼저 두자(나중에 필드 추가 OK)
    private String title;   // 공연명
    private String venue;   // 장소
    private String dateText; // 일자(간단히 문자열로)
}
