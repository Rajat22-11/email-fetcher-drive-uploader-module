package com.finance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import lombok.*;

@Component
@Data
@ConditionalOnProperty(prefix = "google", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConfigurationProperties(prefix = "screener")
public class ScreenerConfig {
    private String sender = "no-reply@screener.in";
    private String subjectContains = "Screener.in Updates";
    private String timezone = "Asia/Kolkata";
    private int daysBack = 3;
}

