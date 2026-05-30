package com.finance.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Component
@ConditionalOnProperty(name = "fetch.scheduler.enabled", havingValue = "true")
@Slf4j
@RequiredArgsConstructor
public class EmailFetchScheduler {

    private final EmailFetcherService emailFetcherService;

    @Scheduled(cron = "0 50 23 * * *", zone = "${screener.timezone:Asia/Kolkata}")
    public void runNightlyFetch() {
        log.info("Scheduled fetch started.",
                kv("schedule", "daily-23-50"));
        try {
            emailFetcherService.processAll();
            log.info("Scheduled fetch completed successfully.",
                    kv("schedule", "daily-23-50"));
        } catch (Exception ex) {
            log.error("Scheduled fetch failed.",
                    kv("schedule", "daily-23-50"),
                    kv("errorMessage", ex.getMessage()));
        }
    }
}
