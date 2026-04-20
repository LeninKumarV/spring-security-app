package com.example.security.security_app.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    // Verification Email Template
    public String buildVerificationEmail(String username,
                                         String token,
                                         String baseUrl) {
        String verifyLink = baseUrl + "/api/auth/verify?token=" + token;

        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Hi %s, verify your email</h2>
                    <p>Thank you for registering. Please verify your email by clicking below:</p>
                    <a href="%s"
                       style="background:#4CAF50; color:white; padding:10px 20px;
                              text-decoration:none; border-radius:5px;">
                        Verify Email
                    </a>
                    <p>This link expires in <strong>24 hours</strong>.</p>
                    <p>If you did not register, ignore this email.</p>
                </body>
                </html>
                """.formatted(username, verifyLink);
    }

    // Password Reset Email Template
    public String buildPasswordResetEmail(String username,
                                          String token,
                                          String baseUrl) {
        String resetLink = baseUrl + "/api/auth/reset-password?token=" + token;

        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Hi %s, reset your password</h2>
                    <p>We received a request to reset your password. Click below to proceed:</p>
                    <a href="%s"
                       style="background:#f44336; color:white; padding:10px 20px;
                              text-decoration:none; border-radius:5px;">
                        Reset Password
                    </a>
                    <p>This link expires in <strong>1 hour</strong>.</p>
                    <p>If you did not request this, ignore this email.</p>
                </body>
                </html>
                """.formatted(username, resetLink);
    }

    // Welcome Email Template
    public String buildWelcomeEmail(String username) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Welcome, %s!</h2>
                    <p>Your email has been verified successfully.</p>
                    <p>You can now log in and start using the application.</p>
                </body>
                </html>
                """.formatted(username);
    }
}