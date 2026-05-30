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

import static net.logstash.logback.argument.StructuredArguments.kv;

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

        log.info("Email fetch start",
                kv("label", label),
                kv("mappingCount", mappings.size()));

        for (Map.Entry<String, String> e : mappings.entrySet()) {
            String from = e.getKey();
            String targetPath = e.getValue();

            try {
                List<EmailMessage> messages = gmailClient.fetchMessagesByLabelAndFrom(label, from);
                if (messages == null || messages.isEmpty()) {
                    log.info("No messages found",
                            kv("label", label),
                            kv("sender", from));
                    continue;
                }

                log.info("Fetched messages",
                        kv("label", label),
                        kv("sender", from),
                        kv("count", messages.size()));

                for (EmailMessage msg : messages) {
                    String filename = msg.getId() + ".eml";
                    try {
                        driveStorage.saveEml(targetPath, filename, msg.getRawEml());
                        log.info("Uploaded eml",
                                kv("label", label),
                                kv("sender", from),
                                kv("messageId", msg.getId()),
                                kv("targetFolder", targetPath));
                    } catch (IOException ioe) {
                        log.error("Upload failed",
                                kv("label", label),
                                kv("sender", from),
                                kv("messageId", msg.getId()),
                                kv("targetFolder", targetPath),
                                kv("error", ioe.getMessage()));
                    }
                }
            } catch (Exception ex) {
                log.error("Fetch failed",
                        kv("label", label),
                        kv("sender", from),
                        kv("error", ex.getMessage()));
            }
        }

        log.info("Email fetch completed",
                kv("label", label));
    }
}
