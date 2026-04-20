package com.example.security.security_app.controllers;

import com.example.security.security_app.models.JwtResponse;
import com.example.security.security_app.models.LoginRequest;
import com.example.security.security_app.models.RegisterRequest;
import com.example.security.security_app.models.UserResponse;
import com.example.security.security_app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless — client discards token
        // Optionally blacklist token in Redis here
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(
            @RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request));
    }
}