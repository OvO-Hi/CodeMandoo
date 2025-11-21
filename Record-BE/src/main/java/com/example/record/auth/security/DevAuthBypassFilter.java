
package com.example.record.auth.security;

import com.example.record.user.User;
import com.example.record.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevAuthBypassFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        // JWT 토큰이 있으면 dev 사용자를 주입하지 않음 (실제 로그인한 사용자 사용)
        String authHeader = req.getHeader("Authorization");
        boolean hasJwtToken = authHeader != null && authHeader.startsWith("Bearer ");

        // SecurityContext에 인증 정보가 없고, JWT 토큰도 없을 때만 dev 사용자 주입
        if (SecurityContextHolder.getContext().getAuthentication() == null && !hasJwtToken) {
            User devUser = userRepository.findById("dev").orElseGet(() -> {
                User u = User.builder()
                        .id("dev")
                        .email("dev@local")
                        .password(passwordEncoder.encode("devpass"))
                        .nickname("DEV")
                        .role("USER")
                        .build();
                return userRepository.save(u);
            });

            var auth = new UsernamePasswordAuthenticationToken(
                    new AuthUser(devUser),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(req, res);
    }
}
