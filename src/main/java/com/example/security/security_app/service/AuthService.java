package com.example.security.security_app.service;

import com.example.security.security_app.entity.User;
import com.example.security.security_app.models.JwtResponse;
import com.example.security.security_app.models.LoginRequest;
import com.example.security.security_app.models.RegisterRequest;
import com.example.security.security_app.models.UserResponse;
import com.example.security.security_app.repositories.UserRepository;
import com.example.security.security_app.security.JwtUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Validated
public class AuthService {

    private final UserRepository       userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils             jwtUtils;
    private final EmailPublisher        emailPublisher;


    @Transactional
    public UserResponse register(@Valid RegisterRequest request) {

        // Duplicate checks
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .roles(CollectionUtils.isEmpty(request.getRoles()) ? Set.of("ROLE_USER")  :
                        request.getRoles())
                .isActive(true)
                .isVerified(false)
                .isLocked(false)
                .isDeleted(false)
                .failedAttempt(0)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {}", saved.getUsername());
        emailPublisher.sendWelcome(request.getEmail(), request.getUsername());
        return toResponse(saved);
    }

    public JwtResponse login(@Valid LoginRequest request) {

        // Fetch user first
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found: " + request.getUsername()));

        // Account status checks
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new DisabledException("Account no longer exists");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new LockedException("Account locked — too many failed attempts");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("Account is inactive");
        }

        // Authenticate
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()));

        } catch (BadCredentialsException e) {
            // Track failed attempts
            int attempts = user.getFailedAttempt() + 1;
            user.setFailedAttempt(attempts);

            if (attempts >= 3) {
                user.setIsLocked(true);
                user.setLockTime(LocalDateTime.now());
                userRepository.save(user);
                throw new LockedException("Account locked after 5 failed attempts");
            }

            userRepository.save(user);
            throw new BadCredentialsException(
                    "Invalid password — you have" + (3 - attempts) + " attempts remaining!");
        }

        // Reset failed attempts on success
        user.setFailedAttempt(0);
        user.setLockTime(null);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate tokens
        String accessToken  = jwtUtils.generateAccessToken(
                user.getUsername(), user.getRoles());
//        String refreshToken = jwtUtils.generateRefreshToken(
//                user.getUsername());

        log.info("User logged in: {}", user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
//                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }


    public JwtResponse refresh(@NotNull String refreshToken) {
        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new AccessDeniedException("Invalid or expired refresh token");
        }

        String username = jwtUtils.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String newAccessToken = jwtUtils.generateAccessToken(
                user.getUsername(), user.getRoles());

        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    private UserResponse toResponse(User user) {
        return getUserResponse(user);
    }

    static UserResponse getUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .isActive(user.getIsActive())
                .isVerified(user.getIsVerified())
                .isLocked(user.getIsLocked())
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

}