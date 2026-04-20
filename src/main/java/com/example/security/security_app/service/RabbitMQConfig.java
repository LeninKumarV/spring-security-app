package com.example.security.security_app.service;

import com.example.security.security_app.models.RabbitMQProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitMQConfig {

    private final RabbitMQProperties props;

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory factory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(factory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public TopicExchange emailExchange() {
        return new TopicExchange(props.getExchange(), true, false);
    }

    @Bean
    public Queue verificationQueue() {
        return QueueBuilder.durable(props.getQueues().getVerification())
                .withArgument("x-dead-letter-exchange", props.getExchange() + ".dlx")
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getVerification() + ".dead")
                .build();
    }

    @Bean
    public Queue resetQueue() {
        return QueueBuilder.durable(props.getQueues().getReset())
                .withArgument("x-dead-letter-exchange", props.getExchange() + ".dlx")
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getReset() + ".dead")
                .build();
    }

    @Bean
    public Queue welcomeQueue() {
        return QueueBuilder.durable(props.getQueues().getWelcome())
                .withArgument("x-dead-letter-exchange", props.getExchange() + ".dlx")
                .withArgument("x-dead-letter-routing-key", props.getRoutingKeys().getWelcome() + ".dead")
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(props.getExchange() + ".dead.queue").build();
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(props.getExchange() + ".dlx", true, false);
    }


    //Even if your email service crashes or throws an exception,
    //the original message is never deleted — it's safely parked in the dead queue for you to handle later using through rabbitMQ UI.
    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with("#");
    }

    @Bean
    public Binding verificationBinding() {
        return BindingBuilder.bind(verificationQueue()).to(emailExchange()).with(props.getRoutingKeys().getVerification());
    }

    @Bean
    public Binding resetBinding() {
        return BindingBuilder.bind(resetQueue()).to(emailExchange()).with(props.getRoutingKeys().getReset());
    }

    @Bean
    public Binding welcomeBinding() {
        return BindingBuilder.bind(welcomeQueue()).to(emailExchange()).with(props.getRoutingKeys().getWelcome());
    }
}