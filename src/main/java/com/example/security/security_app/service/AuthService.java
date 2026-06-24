package com.example.security.security_app.service;

import com.example.security.security_app.entity.User;
import com.example.security.security_app.models.*;
import com.example.security.security_app.repositories.UserRepository;
import com.example.security.security_app.security.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

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
    private final TokenBlacklistService blacklistService;

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
        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            throw new DisabledException(
                    "Email not verified — "
                            + "please check your inbox and verify your email");
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
                throw new LockedException("Account locked after 3 failed attempts");
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
        String refreshToken = jwtUtils.generateRefreshToken(
                user.getUsername());

        log.info("User logged in: {}", user.getUsername());

        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }


    public JwtResponse refresh(String refreshToken) {

        if (!StringUtils.hasText(refreshToken)) {
            throw new AccessDeniedException(
                    "Refresh token is required");
        }

        if (!jwtUtils.isTokenValid(refreshToken)) {
            throw new AccessDeniedException(
                    "Refresh token expired — please login again");
        }

        if (blacklistService.isBlacklisted(refreshToken)) {
            throw new AccessDeniedException(
                    "Refresh token invalidated — please login again");
        }

        String username = jwtUtils.extractUsername(refreshToken);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new DisabledException("Account no longer exists");
        }
        if (Boolean.TRUE.equals(user.getIsLocked())) {
            throw new LockedException("Account is locked");
        }
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new DisabledException("Account is inactive");
        }

        // Generate new access token
        String newAccessToken = jwtUtils.generateAccessToken(
                user.getUsername(),
                user.getRoles());

        log.info("Access token refreshed for: {}", username);

        // Return new access token
        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // reuse same refresh token
                .username(user.getUsername())
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

    public void verifyEmail(String token) {

        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException(
                    "Verification token is required");
        }

        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid verification token — "
                                + "link may have already been used"));

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new DisabledException(
                    "Account no longer exists");
        }

        if (Boolean.TRUE.equals(user.getIsVerified())) {
            throw new IllegalStateException(
                    "Email already verified — please login");
        }

        if (user.getVerificationTokenExpiry() == null) {
            throw new IllegalArgumentException(
                    "Verification token expiry is missing");
        }

        if (!user.getVerificationTokenExpiry()
                .isAfter(LocalDateTime.now())) {

            String newToken = UUID.randomUUID().toString();
            user.setVerificationToken(newToken);
            user.setVerificationTokenExpiry(
                    LocalDateTime.now().plusMinutes(1));
            userRepository.save(user);

            emailPublisher.publishVerificationEmail(
                    user.getEmail(),
                    user.getUsername(),
                    newToken,
                    "1 Minutes"
            );

            System.out.println(user.toString());

            throw new IllegalArgumentException(
                    "Link expired — new link sent to "
                            + user.getEmail());
        }

        user.setIsVerified(true);
        user.setIsActive(true);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);

        log.info("Email verified for: {}", user.getEmail());

        emailPublisher.sendWelcome(
                user.getEmail(),
                user.getFirstName());
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void inviteUser(RegisterRequest request) {

        // Check already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists" + request.getUsername());
        }

        // Generate temp password
        String tempPassword = UUID.randomUUID().toString()
                .substring(0, 8) + "@Tmp";

        // Create user with temp password
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())         // email as username initially
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(passwordEncoder.encode(tempPassword))
                .roles(CollectionUtils.isEmpty(request.getRoles()) ? Set.of("ROLE_USER")  :
                        request.getRoles())
                .isActive(true)
                .isVerified(false)                   // must verify email
                .isLocked(false)
                .isDeleted(false)
                .failedAttempt(0)
                .verificationToken(UUID.randomUUID().toString())
                .verificationTokenExpiry(
                        LocalDateTime.now().plusMinutes(1)) // 48hr to accept invite
                .modifiedBy(UserContext.get() != null
                        ? UserContext.get().getUserName()
                        : "system")
                .build();

        userRepository.save(user);

        // Push invite email to queue
        emailPublisher.publishInviteEmail(
                user.getEmail(),
                user.getUsername(),
                tempPassword,
                user.getVerificationToken());

        log.info("User invited: {} by admin: {}",
                request.getEmail(), UserContext.get().getUserName());
    }

    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "User not found"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);

        emailPublisher.publishPasswordResetEmail(
                user.getEmail(),
                user.getUsername(),
                token);
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid reset token"));

        if (user.getResetTokenExpiry()
                .isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException(
                    "Reset token expired — request a new one");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    public void logout(String accessToken, String refreshToken) {

        if (StringUtils.hasText(accessToken)) {
            try {
                Claims claims = jwtUtils
                        .parseClaimsIgnoreExpiry(accessToken);

                long expiry    = claims.getExpiration().getTime();
                long now       = System.currentTimeMillis();
                long remaining = expiry - now;

                if (remaining > 0) {
                    // token still valid — blacklist remaining time
                    blacklistService.blacklistToken(
                            accessToken, remaining);
                } else {
                    // already expired — blacklist for small buffer
                    blacklistService.blacklistToken(
                            accessToken, 60000); // 1 min buffer
                }

                log.info("Access token blacklisted");

            } catch (Exception e) {
                log.warn("Could not blacklist access token: {}",
                        e.getMessage());
            }
        }

        if (StringUtils.hasText(refreshToken)) {
            System.out.println("Refresh token blacklisted Check");
            try {
                Claims claims = jwtUtils
                        .parseClaimsIgnoreExpiry(refreshToken);

                long expiry    = claims.getExpiration().getTime();
                long now       = System.currentTimeMillis();
                long remaining = expiry - now;

                if (remaining > 0) {
                    blacklistService.blacklistToken(
                            refreshToken, remaining);
                } else {
                    // already expired — blacklist for small buffer
                    blacklistService.blacklistToken(
                            refreshToken, 60000); // 1 min buffer
                }

                log.info("Refresh token blacklisted");

            } catch (Exception e) {
                log.warn("Could not blacklist refresh token: {}",
                        e.getMessage());
            }
        }
    }
}