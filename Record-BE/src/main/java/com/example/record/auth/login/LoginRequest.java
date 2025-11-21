
package com.example.record.auth.login;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 3, max = 15, message = "아이디는 3~15자로 입력해주세요.")
    private String id;   // 아이디로 로그인

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상으로 입력해주세요.")
    private String password;   // 비밀번호
}
