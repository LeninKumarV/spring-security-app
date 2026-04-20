package com.example.security.security_app.service;

import com.example.security.security_app.models.EmailRequest;
import com.example.security.security_app.models.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public void sendVerification(String to, String link, String username) {
        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getVerification(),
                EmailRequest.builder()
                        .to(to)
                        .subject("Verify your email")
                        .body(link)
                        .username(username)
                        .build()
        );
    }

    public void sendReset(String to, String link, String username) {
        rabbitTemplate.convertAndSend(
                props.getExchange(),
                props.getRoutingKeys().getReset(),
                EmailRequest.builder()
                        .to(to)
                        .subject("Reset your password")
                        .body(link)
                        .username(username)
                        .build()
        );
    }

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
}