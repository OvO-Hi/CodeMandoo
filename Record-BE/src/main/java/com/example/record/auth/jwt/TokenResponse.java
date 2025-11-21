
package com.example.record.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;     // 순수 JWT
    private String type;      // "Bearer"
    private long   expiresIn; // ms
    private String role;      // USER / ADMIN
}
