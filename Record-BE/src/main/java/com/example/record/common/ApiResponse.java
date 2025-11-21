package com.example.record.common;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 공통 API 응답 클래스
 * 
 * 역할: 프론트엔드와 백엔드 간의 응답 형식을 통일하기 위한 래퍼 클래스
 * 
 * 사용 목적:
 * - 프론트엔드의 apiClient.ts가 기대하는 success, data, message 구조를 제공
 * - 모든 API 응답을 일관된 형식으로 반환하여 프론트엔드 처리 로직 단순화
 * 
 * 필드 설명:
 * - success: 요청 성공 여부 (true/false)
 * - data: 실제 응답 데이터 (제네릭 타입 T로 다양한 타입 지원)
 * - message: 응답 메시지 (성공/실패 메시지)
 * 
 * 사용 예시:
 * - 성공 시: new ApiResponse<>(true, tokenResponse, "회원가입 성공")
 * - 실패 시: new ApiResponse<>(false, null, "이미 사용 중인 이메일입니다.")
 */
@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
}

