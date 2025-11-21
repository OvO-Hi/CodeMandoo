// ApiKeyInterceptor: 모든 HTTP 요청에 대해 Authorization 헤더의 API 키 유효성을 검사하는 Spring 인터셉터입니다.

package com.example.record;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    @Autowired
    private ApiKeyRepository apiKeyRepository; // API 키 유효성 검사를 위한 DB 접근 객체

    // 컨트롤러 실행 전에 호출되어 요청을 사전 처리
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Authorization 헤더 추출
        String header = request.getHeader("Authorization");
        System.out.println("=== Authorization 헤더: " + header); // 디버깅용 로그

        // 헤더가 없거나 Bearer 형식이 아닌 경우
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println(">>> Authorization 헤더 누락 또는 잘못된 형식");
            try {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or invalid Authorization header");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false; // 요청 차단
        }

        // "Bearer " 접두사 제거 후 API 키만 추출
        String apiKey = header.substring(7);
        System.out.println("=== 추출된 API 키: " + apiKey);

        // DB에서 해당 API 키 존재 여부 확인
        boolean exists = apiKeyRepository.existsByApiKey(apiKey);
        System.out.println("=== DB에 키 존재 여부: " + exists);

        // 키가 유효하지 않으면 401 응답 반환
        if (!exists) {
            System.out.println(">>> 존재하지 않는 API 키");
            try {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid API Key");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        // 검증 통과 → 컨트롤러로 요청 진행 허용
        return true;
    }
}
