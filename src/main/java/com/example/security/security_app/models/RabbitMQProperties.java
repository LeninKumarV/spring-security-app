package com.example.security.security_app.models;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private String exchange;
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    @Getter
    @Setter
    public static class Queues {
        private String verification;
        private String reset;
        private String welcome;
        private String invite;
    }

    @Getter
    @Setter
    public static class RoutingKeys {
        private String verification;
        private String reset;
        private String welcome;
        private String invite;
    }
}
