package com.phegon.FoodApp;

import com.phegon.FoodApp.email_notification.dtos.NotificationDTO;
import com.phegon.FoodApp.email_notification.services.NotificationService;
import com.phegon.FoodApp.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
//@RequiredArgsConstructor
public class FoodAppApplication {

//	private final NotificationService notificationService;

	public static void main(String[] args) {
		SpringApplication.run(FoodAppApplication.class, args);
	}

//	@Bean
//	CommandLineRunner runner() {
//		return  args -> {
//			// Example usage of NotificationService
//			// notificationService.sendEmail(new NotificationDTO("
//			NotificationDTO notificationDTO = NotificationDTO.builder()
//					.recipient("hovu1000@gmail.com")
//					.subject("Hello from FoodApp")
//					.body("This is a test email from FoodApp.")
//					.type(NotificationType.EMAIL)
//					.build();
//			notificationService.sendEmail(notificationDTO);
//		};
//	}

}
