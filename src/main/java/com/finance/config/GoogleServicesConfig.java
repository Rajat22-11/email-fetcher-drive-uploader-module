package com.finance.config;

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.gmail.Gmail;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "google", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoogleServicesConfig {

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Arrays.asList(
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/drive.file"
    );

    // Injecting your clean config properties class here
    private final GoogleOAuthConfig config;

    @Bean
    public com.google.api.client.auth.oauth2.Credential googleCredential() throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        // Resolve path dynamically from your properties (stripping 'classpath:' if needed)
        String resourcePath = config.getClientSecretFile().replace("classpath:", "");
        if (!resourcePath.startsWith("/")) {
            resourcePath = "/" + resourcePath;
        }

        InputStream in = getClass().getResourceAsStream(resourcePath);
        if (in == null) {
            throw new RuntimeException("OAuth client secret file not found at: " + config.getClientSecretFile());
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(config.getTokensDirectoryPath())))
                .setAccessType("offline")
                .build();

        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }

    @Bean
    public Gmail gmailService(com.google.api.client.auth.oauth2.Credential credential) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Gmail.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(config.getApplicationName())
                .build();
    }

    @Bean
    public Drive driveService(com.google.api.client.auth.oauth2.Credential credential) throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(config.getApplicationName())
                .build();
    }
}