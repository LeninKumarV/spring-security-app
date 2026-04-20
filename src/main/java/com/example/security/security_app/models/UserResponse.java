package com.example.security.security_app.models;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String tenantId;
    private String subdomain;
    private Set<String> roles;
    private Boolean isActive;
    private Boolean isVerified;
    private Boolean isLocked;
    private LocalDateTime lastLogin;
    private Instant createdAt;
    private Instant updatedAt;
}
