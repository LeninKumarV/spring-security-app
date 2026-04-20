package com.example.security.security_app.service;

import com.example.security.security_app.models.EmailRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailTemplateService templateService;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.base-url}")
    private String baseUrl;

    // ─── Send Verification Email ──────────────────────────
    public void sendVerificationEmail(String toEmail,
                                      String username,
                                      String token) {
        String subject = "Verify your email address";
        String body    = templateService
                .buildVerificationEmail(username, token, baseUrl);

        sendEmail(EmailRequest.builder()
                .to(toEmail)
                .subject(subject)
                .body(body)
                .build());

        log.info("Verification email sent to: {}", toEmail);
    }

    // ─── Send Password Reset Email ────────────────────────
    public void sendPasswordResetEmail(String toEmail,
                                       String username,
                                       String token) {
        String subject = "Reset your password";
        String body    = templateService
                .buildPasswordResetEmail(username, token, baseUrl);

        sendEmail(EmailRequest.builder()
                .to(toEmail)
                .subject(subject)
                .body(body)
                .build());

        log.info("Password reset email sent to: {}", toEmail);
    }

    // ─── Send Welcome Email ───────────────────────────────
    public void sendWelcomeEmail(String toEmail, String username) {
        String subject = "Welcome to the platform!";
        String body    = templateService.buildWelcomeEmail(username);

        sendEmail(EmailRequest.builder()
                .to(toEmail)
                .subject(subject)
                .body(body)
                .build());

        log.info("Welcome email sent to: {}", toEmail);
    }

    // ─── Core Send Method ─────────────────────────────────
    private void sendEmail(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getBody(), true); // true = HTML

            mailSender.send(message);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}",
                    request.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}