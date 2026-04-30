package com.example.security.security_app.controllers;

import com.example.security.security_app.models.JwtResponse;
import com.example.security.security_app.models.LoginRequest;
import com.example.security.security_app.models.RegisterRequest;
import com.example.security.security_app.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // Public — verify email from invite link
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified — you can now login");
    }

    // Admin only — invite user
    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> inviteUser(
            @RequestBody @Valid RegisterRequest request) {
        authService.inviteUser(request);
        return ResponseEntity.ok("Invitation sent to " + request.getEmail());
    }

//    // ✅ Public — forgot password
//    @PostMapping("/forgot-password")
//    public ResponseEntity<String> forgotPassword(
//            @RequestParam String email) {
//        authService.forgotPassword(email);
//        return ResponseEntity.ok("Reset email sent");
//    }
//
//    // ✅ Public — reset password
//    @PostMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(
//            @RequestParam String token,
//            @RequestParam String newPassword) {
//        authService.resetPassword(token, newPassword);
//        return ResponseEntity.ok("Password reset successfully");
//    }

}