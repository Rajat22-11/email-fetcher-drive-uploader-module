package com.finance.gmail;

import com.finance.config.ScreenerConfig;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "google", name = "enabled", havingValue = "true", matchIfMissing = true)
public class GoogleGmailClient implements GmailClient {
    private final Gmail gmail;
    private final ScreenerConfig screenerConfig;


    @Override
    public List<EmailMessage> fetchMessagesByLabelAndFrom(String label, String from) throws Exception {
        String query = buildQuery(label, from);
        log.info("Searching Gmail with query: {}", query);

        List<EmailMessage> results = new ArrayList<>();
        String pageToken = null;

        do {
            ListMessagesResponse response = gmail.users().messages().list("me")
                    .setQ(query)
                    .setPageToken(pageToken)
                    .setMaxResults(500L)
                    .execute();

            List<Message> messages = response.getMessages();
            if (messages != null) {
                for (Message message : messages) {
                    Message fullMessage = gmail.users().messages().get("me", message.getId())
                            .setFormat("raw")
                            .execute();
                    String raw = fullMessage.getRaw();
                    if (raw == null || raw.isBlank()) {
                        continue;
                    }
                    byte[] emlBytes = Base64.getUrlDecoder().decode(raw);
                    results.add(new EmailMessage(message.getId(), emlBytes));
                }
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null && !pageToken.isBlank());

        log.info("Fetched {} raw EML messages for query {}", results.size(), query);
        return results;
    }

    private String buildQuery(String label, String from) {
        List<String> clauses = new ArrayList<>();
        if (label != null && !label.isBlank()) {
            clauses.add("label:" + label);
        }
        if (from != null && !from.isBlank()) {
            clauses.add("from:" + from);
        }
        if (screenerConfig.getSubjectContains() != null && !screenerConfig.getSubjectContains().isBlank()) {
            clauses.add("subject:\"" + screenerConfig.getSubjectContains().replace("\"", "") + "\"");
        }

        var zone = java.time.ZoneId.of(Objects.requireNonNullElse(screenerConfig.getTimezone(), "Asia/Kolkata"));
        var today = java.time.LocalDate.now(zone);
        var yesterday = today.minusDays(Math.max(1, screenerConfig.getDaysBack()));
        var formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy/MM/dd");
        clauses.add("after:" + yesterday.format(formatter));
        clauses.add("before:" + today.format(formatter));

        return String.join(" ", clauses);
    }
}


