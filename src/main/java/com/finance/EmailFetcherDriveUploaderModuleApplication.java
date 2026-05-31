package com.finance;

import com.finance.service.EmailFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import static net.logstash.logback.argument.StructuredArguments.kv;

@SpringBootApplication
@Slf4j
public class EmailFetcherDriveUploaderModuleApplication {

	private final EmailFetcherService emailFetcherService;

	// Direct Constructor Injection
	public EmailFetcherDriveUploaderModuleApplication(EmailFetcherService emailFetcherService) {
		this.emailFetcherService = emailFetcherService;
	}

	public static void main(String[] args) {
		SpringApplication.run(EmailFetcherDriveUploaderModuleApplication.class, args);
	}

	/**
	 * This listener triggers automatically exactly once when the Spring Boot
	 * context is fully initialized and ready to serve traffic on Render.
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void runSyncOnStartup() {
		log.info("Application context fully loaded. Starting automated email-fetch and drive-upload pipeline.",
				kv("trigger", "application-ready-startup"));
		try {
			emailFetcherService.processAll();
			log.info("Automated startup pipeline executed successfully.",
					kv("trigger", "application-ready-startup"),
					kv("status", "SUCCESS"));
		} catch (Exception ex) {
			log.error("Automated startup pipeline hit an unhandled exception.",
					kv("trigger", "application-ready-startup"),
					kv("errorMessage", ex.getMessage()),
					kv("errorType", ex.getClass().getSimpleName()));
		}
	}
}