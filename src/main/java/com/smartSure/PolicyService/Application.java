package com.smartSure.PolicyService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication

@EnableJpaAuditing
@EnableScheduling
@EnableAsync                     // ← add this for NotificationService
@EnableFeignClients              // ← add this for future AuthService Feign calls
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
