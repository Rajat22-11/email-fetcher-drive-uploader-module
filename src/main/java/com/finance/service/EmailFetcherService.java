package com.finance.service;

import com.finance.config.MailMappingConfig;
import com.finance.gmail.EmailMessage;
import com.finance.gmail.GmailClient;
import com.finance.storage.DriveStorage;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailFetcherService {
    private final MailMappingConfig mappingConfig;
    private final GmailClient gmailClient;
    private final DriveStorage driveStorage;


    /**
     * Process configured mappings: for each sender configured, fetch messages from the configured label
     * and store each message as an .eml file into the mapped Google Drive folder ID.
     */
    public void processAll() {
        String label = mappingConfig.getLabel();
        Map<String, String> mappings = mappingConfig.getMappings();

        log.info("Starting processing for label={} with {} mappings", label, mappings.size());

        for (Map.Entry<String, String> e : mappings.entrySet()) {
            String from = e.getKey();
            String targetPath = e.getValue();

            try {
                List<EmailMessage> messages = gmailClient.fetchMessagesByLabelAndFrom(label, from);
                if (messages == null || messages.isEmpty()) {
                    log.info("No messages for {} under label {}", from, label);
                    continue;
                }

                for (EmailMessage msg : messages) {
                    String filename = msg.getId() + ".eml";
                    try {
                        driveStorage.saveEml(targetPath, filename, msg.getRawEml());
                    } catch (IOException ioe) {
                        log.error("Failed to save message {} to {}", msg.getId(), targetPath, ioe);
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to fetch messages for {} under label {}", from, label, ex);
            }
        }

        log.info("Processing completed for label={}", label);
    }
}
