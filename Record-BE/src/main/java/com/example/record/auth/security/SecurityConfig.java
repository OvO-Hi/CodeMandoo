package com.example.record.auth.security;

import com.example.record.auth.jwt.JwtAuthenticationFilter;
import com.example.record.auth.jwt.JwtUtil;
import com.example.record.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final AuthenticationEntryPoint authEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable()) // 프론트에서만 필요하다면 disable 가능
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // ====== 공개 허용 구간 ======
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()

                        .requestMatchers("/STTorText/**").permitAll()

                        .requestMatchers("/ocr/**").permitAll()
                        .requestMatchers("/generate-image/**").permitAll()
                        .requestMatchers("/generate-image/with-file").permitAll()
                        .requestMatchers("/review-questions/**").permitAll()
                        .requestMatchers("/reviews/**").permitAll()
                        .requestMatchers("/review/**").permitAll()





                        // 편의용 테스트 엔드포인트
                        .requestMatchers("/test").permitAll()
                        .requestMatchers("/api/test/**").permitAll()

                        // ====== 나머지는 JWT 필요 ======
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex.authenticationEntryPoint(authEntryPoint));

        // ===== JWT 인증 필터 추가 =====
        http.addFilterBefore(
                new JwtAuthenticationFilter(jwtUtil, userRepository),
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }
}
