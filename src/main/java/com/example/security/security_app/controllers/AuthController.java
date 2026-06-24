package com.example.security.security_app.controllers;

import com.example.security.security_app.models.JwtResponse;
import com.example.security.security_app.models.LoginRequest;
import com.example.security.security_app.models.RegisterRequest;
import com.example.security.security_app.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private Cookie expiredCookie;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(
            @CookieValue(name = "refreshToken",
                    required = false)
            String refreshToken,
            HttpServletResponse response) {

        if (refreshToken == null) {
            throw new AccessDeniedException(
                    "Refresh token missing — please login again");
        }

        JwtResponse jwtResponse = authService.refresh(refreshToken);

        Cookie newRefreshCookie = new Cookie(
                "refreshToken",
                jwtResponse.getRefreshToken());
        newRefreshCookie.setHttpOnly(true);
        newRefreshCookie.setSecure(true);
        newRefreshCookie.setPath("/api/auth/refresh");
        newRefreshCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(newRefreshCookie);

        return ResponseEntity.ok(
                JwtResponse.builder()
                        .accessToken(jwtResponse.getAccessToken())
                        .username(jwtResponse.getUsername())
                        .roles(jwtResponse.getRoles())
                        .build()
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader("Authorization")
            String authHeader,
            @RequestHeader(value = "X-Refresh-Token",
                    required = false)
            String headerRefreshToken,
            @CookieValue(name = "refreshToken",
                    required = false)
            String cookieRefreshToken,
            HttpServletResponse response) {

        String accessToken = null;
        if (StringUtils.hasText(authHeader)
                && authHeader.startsWith("Bearer ")) {
            accessToken = authHeader.substring(7);
        }

        String refreshToken = StringUtils.hasText(headerRefreshToken)
                ? headerRefreshToken    // Postman sends this
                : cookieRefreshToken;   // Browser sends this

        authService.logout(accessToken, refreshToken);

        Cookie expiredCookie = new Cookie("refreshToken", "");
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(false); // false for localhost
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    // verify email from invite link
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Email verified — you can now login");
    }

    // invite user
    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> inviteUser(
            @RequestBody @Valid RegisterRequest request) {
        authService.inviteUser(request);
        return ResponseEntity.ok("Invitation sent to " + request.getEmail());
    }

    // forgot password
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(
            @RequestParam String email) {
        authService.forgotPassword(email);
        return ResponseEntity.ok("Reset email sent");
    }

    // reset password
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String token,
            @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password reset successfully");
    }

}