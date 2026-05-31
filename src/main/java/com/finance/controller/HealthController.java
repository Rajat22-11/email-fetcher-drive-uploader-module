package com.finance.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@RestController
@RequiredArgsConstructor
@Slf4j
public class HealthController {

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

        return ResponseEntity.ok(body);
    }
}