package com.example.security.security_app.service;

import com.example.security.security_app.entity.EmailType;
import com.example.security.security_app.models.EmailMessage;
import com.example.security.security_app.models.EmailRequest;
import com.example.security.security_app.models.RabbitMQProperties;
import com.example.security.security_app.models.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public void sendWelcome(String to, String username) {
        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getWelcome(),
                EmailRequest.builder()
                        .to(to)
                        .subject("Welcome!")
                        .body("Thanks for joining.")
                        .username(username)
                        .build()
        );
    }

    // Publish Invite Email
    public void  publishInviteEmail(String toEmail,
                                   String userName,
                                   String tempPassword,
                                   String token) {

        EmailMessage message = EmailMessage.builder()
                .toEmail(toEmail)
                .username(userName)
                .tempPassword(tempPassword)
                .token(token)
                .emailType(EmailType.INVITE)
                .invitedBy(UserContext.get() != null
                        ? UserContext.get().getUserName()
                        : "system")
                .requestId(UserContext.get() != null
                        ? UserContext.get().getRequestId()
                        : UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getInvite(),
                message);

        log.info("Invite email queued for: {} by: {}",
                toEmail,
                message.getInvitedBy());
    }

    // Publish Verification Email
    public void publishVerificationEmail(String toEmail,
                                         String userName,
                                         String token,
                                         String emailExpiry) {
        EmailMessage message = EmailMessage.builder()
                .toEmail(toEmail)
                .username(userName)
                .token(token)
                .emailType(EmailType.VERIFICATION)
                .requestId(UserContext.get() != null
                        ? UserContext.get().getRequestId()
                        : UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .emailExpiry(emailExpiry)
                .build();

        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getVerification(),
                message);

        log.info("Verification email queued for: {}", toEmail);
    }

    // Publish Password Reset Email
    public void publishPasswordResetEmail(String toEmail,
                                          String userName,
                                          String token) {
        EmailMessage message = EmailMessage.builder()
                .toEmail(toEmail)
                .username(userName)
                .token(token)
                .emailType(EmailType.PASSWORD_RESET)
                .requestId(UserContext.get() != null
                        ? UserContext.get().getRequestId()
                        : UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getReset(),
                message);

        log.info("Password reset email queued for: {}", toEmail);
    }
}