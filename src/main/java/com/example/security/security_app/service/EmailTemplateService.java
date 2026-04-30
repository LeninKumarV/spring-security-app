package com.example.security.security_app.service;

import org.springframework.stereotype.Service;

@Service
public class EmailTemplateService {

    // Verification Email Template
    public String buildVerificationEmail(String username,
                                         String token,
                                         String baseUrl,
                                         String emailExpiry) {
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
                    <p>This link expires in <strong>%s</strong>.</p>
                    <p>If you did not register, ignore this email.</p>
                </body>
                </html>
                """.formatted(username, verifyLink, emailExpiry);
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

    // Invite Email Template
    public String buildInviteEmail(String userName,
                                   String tempPassword,
                                   String token,
                                   String invitedBy,
                                   String baseUrl) {

        String verifyLink = baseUrl
                + "/api/auth/verify?token=" + token;

        return """
                <html>
                <body style="font-family: Arial, sans-serif;
                             padding: 20px;
                             background-color: #f4f4f4;">

                    <div style="max-width: 600px;
                                margin: auto;
                                background: white;
                                padding: 30px;
                                border-radius: 8px;">

                        <h2 style="color: #333;">
                            Hi %s, you've been invited! 🎉
                        </h2>

                        <p>You have been invited by
                           <strong>%s</strong>
                           to join the platform.</p>

                        <p>Here are your temporary credentials:</p>

                        <div style="background: #f8f8f8;
                                    padding: 15px;
                                    border-radius: 5px;
                                    margin: 20px 0;">
                            <p><strong>UserName:</strong> %s</p>
                            <p><strong>Password:</strong> %s</p>
                        </div>

                        <p>Click below to verify your email
                           and activate your account:</p>

                        <a href="%s"
                           style="display: inline-block;
                                  background: #4CAF50;
                                  color: white;
                                  padding: 12px 24px;
                                  text-decoration: none;
                                  border-radius: 5px;
                                  margin: 10px 0;">
                            Accept Invitation
                        </a>

                        <p style="color: #999; font-size: 12px;">
                            This invitation expires in
                            <strong>48 hours</strong>.
                            Please change your password after first login.
                        </p>

                        <p style="color: #999; font-size: 12px;">
                            If you did not expect this invitation,
                            ignore this email.
                        </p>
                    </div>
                </body>
                </html>
                """.formatted(
                userName,
                invitedBy,
                userName,
                tempPassword,
                verifyLink);
    }
}