package com.example.security.security_app.service;

import com.example.security.security_app.entity.User;
import com.example.security.security_app.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.username}")
    private String adminUsername;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {

        if (userRepository.existsByUsername(adminUsername)) {
            log.info("Admin user already exists — skipping initialization");
            return;
        }

        User admin = User.builder()
                .username(adminUsername)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .firstName("Lenin")
                .lastName("Admin")
                .roles(Set.of("ROLE_ADMIN"))
                .isActive(true)
                .isVerified(true)
                .isLocked(false)
                .isDeleted(false)
                .failedAttempt(0)
                .build();

        userRepository.save(admin);
        log.info("Default admin user created: {}", adminUsername);
    }
}