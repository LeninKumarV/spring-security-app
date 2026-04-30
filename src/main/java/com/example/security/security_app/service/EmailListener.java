package com.example.security.security_app.service;

import com.example.security.security_app.models.EmailMessage;
import com.example.security.security_app.models.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailListener {

    private final EmailService  emailService;

    @RabbitListener(queues = "${app.rabbitmq.queues.verification}")
    public void handleVerification(EmailMessage message) {
        try{
            log.info("Verification email received for: {}", message.getToEmail());
            emailService.sendVerificationEmail(message.getToEmail(), message.getUsername(), message.getToken(), message.getEmailExpiry());
        }
        catch (Exception e){
            log.info("Got error while verify the invite user : {} {}", message.getToEmail(), e.getMessage());
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.reset}")
    public void handleReset(EmailRequest message) {
        log.info("Password reset email received for: {}", message.getTo());
    }

    @RabbitListener(queues = "${app.rabbitmq.queues.welcome}")
    public void handleWelcome(EmailRequest message) {
        log.info("Welcome email received for: {}", message.getTo());
        try{
            emailService.sendWelcomeEmail(message.getTo(), message.getUsername());
        }
        catch (Exception e){
            log.info("Got error while received welcome email : {} {}", message.getTo(), e.getMessage());
        }
    }

    // Consume Invite Queue
    @RabbitListener(queues = "${app.rabbitmq.queues.invite}")
    public void consumeInviteEmail(EmailMessage message) {
        log.info("Consuming invite email for: {} [requestId={}]",
                message.getToEmail(), message.getRequestId());
        try {
            emailService.sendInviteUserEmail(message);
            log.info("Invite email sent to: {}", message.getToEmail());

        } catch (Exception e) {
            log.error("Failed to send invite email to {}: {}",
                    message.getToEmail(), e.getMessage());
            throw e; // triggers retry → dead letter
        }
    }

    @RabbitListener(queues = "email.exchange.dead.queue")
    public void handle(Message message) {
        log.error("Dead letter received. Routing key: {}, Body: {}",
                message.getMessageProperties().getReceivedRoutingKey(),
                new String(message.getBody()));
        // alert, store to DB, or trigger manual review
    }
}
