package com.example.security.security_app.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {

    private String accessToken;
    private String refreshToken;
    private String username;
    private String tenantId;
    private String email;
    private Set<String> roles;
}
