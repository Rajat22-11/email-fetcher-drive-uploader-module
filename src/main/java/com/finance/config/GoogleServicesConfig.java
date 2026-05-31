package com.finance.config;

import com.google.api.client.auth.oauth2.Credential;
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
    public Credential googleCredential() throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = null;
        String secretPath = config.getClientSecretFile();

        // 1. Try reading it as an external absolute file system path first (For Render)
        java.io.File externalFile = new java.io.File(secretPath);
        if (externalFile.exists()) {
            in = new java.io.FileInputStream(externalFile);
        } else {
            // 2. Fallback: Clean up prefix and read from Classpath (For Local Machine)
            String resourcePath = secretPath.replace("classpath:", "");
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            in = getClass().getResourceAsStream(resourcePath);
        }

        if (in == null) {
            throw new RuntimeException("OAuth client secret file not found at path or classpath: " + secretPath);
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