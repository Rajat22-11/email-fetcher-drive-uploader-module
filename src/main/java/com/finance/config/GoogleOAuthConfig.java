package com.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.Data;

@Component
@Data
@ConditionalOnProperty(
        prefix = "google",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
@ConfigurationProperties(prefix = "google")
public class GoogleOAuthConfig {

    /**
     * Location of the Google OAuth2 client secrets JSON file.
     * Maps to: src/main/resources/client_secret.json
     */
    private String clientSecretFile = "classpath:client_secret.json";

    /**
     * Local directory where your permanent refresh tokens will be stored.
     */
    private String tokensDirectoryPath = "tokens";

    /**
     * Application identifier passed to Google API headers.
     */
    private String applicationName = "Email Fetcher Drive Uploader";
}