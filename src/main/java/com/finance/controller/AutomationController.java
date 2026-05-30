package com.finance.controller;

import com.finance.service.EmailFetcherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
@RequiredArgsConstructor
@Slf4j
public class AutomationController {

    private final EmailFetcherService emailFetcherService;

    @Value("${spring.application.name:email-fetcher-drive-uploader-module}")
    private String applicationName;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Instant now = Instant.now();

        log.info("Health check received.",
                kv("endpoint", "/health"),
                kv("application", applicationName),
                kv("uptimeMs", uptimeMs));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", "UP");
        body.put("application", applicationName);
        body.put("timestamp", now.toString());
        body.put("uptimeMs", uptimeMs);

        log.info("Health check completed.",
                kv("endpoint", "/health"),
                kv("status", "UP"),
                kv("application", applicationName));

        return ResponseEntity.ok(body);
    }

    @PostMapping("/api/cron/fetch-screener")
    public ResponseEntity<Map<String, Object>> triggerFetch() {
        Instant startedAt = Instant.now();

        log.info("External cron trigger received.",
                kv("endpoint", "/api/cron/fetch-screener"),
                kv("application", applicationName),
                kv("trigger", "github-actions-daily"));

        try {
            emailFetcherService.processAll();

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "SUCCESS");
            body.put("message", "Gmail to Drive synchronization executed successfully.");
            body.put("trigger", "github-actions-daily");
            body.put("startedAt", startedAt.toString());
            body.put("completedAt", Instant.now().toString());

            log.info("External cron trigger completed successfully.",
                    kv("endpoint", "/api/cron/fetch-screener"),
                    kv("application", applicationName),
                    kv("trigger", "github-actions-daily"),
                    kv("status", "SUCCESS"));

            return ResponseEntity.ok(body);
        } catch (Exception ex) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("status", "ERROR");
            body.put("message", "Execution failed: " + ex.getMessage());
            body.put("trigger", "github-actions-daily");
            body.put("startedAt", startedAt.toString());
            body.put("completedAt", Instant.now().toString());

            log.error("External cron trigger failed.",
                    kv("endpoint", "/api/cron/fetch-screener"),
                    kv("application", applicationName),
                    kv("trigger", "github-actions-daily"),
                    kv("error", ex.getMessage()),
                    kv("errorType", ex.getClass().getSimpleName()));

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }
}


