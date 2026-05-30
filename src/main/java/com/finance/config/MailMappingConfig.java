package com.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Component
@Data
@ConfigurationProperties(prefix = "mail")
public class MailMappingConfig {

    /**
     * Gmail label to scan (e.g. SCREENER)
     */
    private String label = "Screener";
    /**
     * Mapping of sender email -> Google Drive folder ID where EMLs should be stored
     */
    private Map<String, String> mappings = new HashMap<>();
}
