package com.example.record.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    // GET /admin/dashboard 요청 시 관리자용 메시지 반환
    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "관리자 전용 페이지입니다.";
    }
}
