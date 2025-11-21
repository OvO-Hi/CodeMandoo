package com.example.record.auth.forgot;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ForgotIdRequest {
    @NotBlank @Email
    private String email;
}

