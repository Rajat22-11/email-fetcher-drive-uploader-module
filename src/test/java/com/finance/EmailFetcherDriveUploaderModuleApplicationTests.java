package com.finance;

import com.finance.gmail.GmailClient;
import com.finance.storage.DriveStorage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

@SpringBootTest(properties = "google.enabled=false")
class EmailFetcherDriveUploaderModuleApplicationTests {

	@Test
	void contextLoads() {
	}

	@TestConfiguration
	static class TestBeans {
		@Bean
		GmailClient gmailClient() {
			return (label, from) -> java.util.Collections.emptyList();
		}

		@Bean
		DriveStorage driveStorage() {
			return (folderId, filename, content) -> { };
		}
	}

}
