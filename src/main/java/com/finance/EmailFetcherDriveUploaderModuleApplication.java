package com.finance;

import com.finance.service.EmailFetcherService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EmailFetcherDriveUploaderModuleApplication {

	public static void main(String[] args) {
		SpringApplication.run(EmailFetcherDriveUploaderModuleApplication.class, args);
	}

	/**
	 * This tells Spring Boot to execute your fetcher logic
	 * automatically right after startup finishes.
	 */
	@Bean
	public CommandLineRunner executeOnStartup(EmailFetcherService emailFetcherService) {
		return args -> {
			System.out.println(">>> App initialized. Initiating email fetch sequence...");
			try {
				emailFetcherService.processAll();
				System.out.println(">>> Execution finished successfully.");
			} catch (Exception e) {
				System.err.println(">>> Error during execution:");
				e.printStackTrace();
			}
		};
	}
}