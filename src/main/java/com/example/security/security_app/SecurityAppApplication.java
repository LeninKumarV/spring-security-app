package com.example.security.security_app;

import com.example.security.security_app.models.RabbitMQProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(RabbitMQProperties.class)
public class SecurityAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecurityAppApplication.class, args);
	}

}
