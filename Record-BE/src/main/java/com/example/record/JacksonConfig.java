package com.example.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * Jackson 설정 클래스
 * 
 * 역할: Java 8 날짜/시간 타입(LocalDateTime 등)을 JSON으로 직렬화/역직렬화할 수 있도록 설정
 * 
 * 문제 해결:
 * - LocalDateTime을 JSON으로 변환할 때 발생하는 오류 해결
 * - JavaTimeModule을 등록하여 LocalDateTime, LocalDate, LocalTime 등을 지원
 * 
 * 설정 내용:
 * - JavaTimeModule 등록: Java 8 날짜/시간 타입 지원
 * - write-dates-as-timestamps: false → ISO-8601 형식 문자열로 변환 (예: "2024-11-13T09:50:13")
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper 빈 설정
     * 
     * @Primary: 여러 ObjectMapper 빈이 있을 때 이 빈을 우선적으로 사용
     * 
     * JavaTimeModule을 등록하여 LocalDateTime, LocalDate, LocalTime 등을 JSON으로 변환할 수 있게 함
     * writeDatesAsTimestamps(false)로 설정하여 날짜를 타임스탬프가 아닌 ISO-8601 형식 문자열로 변환
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
        return builder
                .modules(new JavaTimeModule())  // Java 8 날짜/시간 타입 지원 모듈 등록
                .build()
                .configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);  // 날짜를 타임스탬프가 아닌 ISO-8601 문자열로 변환
    }
}

