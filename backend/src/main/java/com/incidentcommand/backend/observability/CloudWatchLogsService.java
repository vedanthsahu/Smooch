package com.incidentcommand.backend.observability;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.FilterLogEventsRequest;

/**
 * Read-only, rate-limited window into the Production Simulator's real
 * CloudWatch Logs (see infra/aws/cloudwatch/production-simulator-logging.json)
 * -- a demo feature, not a log explorer. The read credential is held here,
 * server-side, and never handed to the frontend: a demo viewer only ever
 * talks to this backend's own /api/logs/simulator endpoint.
 *
 * Deliberately always bounds the query by time: an unbounded
 * FilterLogEvents call returned nothing even with real events present
 * during manual verification, so a start-time window isn't just a nicety
 * here, it's load-bearing.
 *
 * Failures are logged and swallowed, not thrown -- same tolerance-for-AWS-
 * not-being-configured-yet posture as S3StorageService.
 */
@Service
public class CloudWatchLogsService {

    private static final Logger log = LoggerFactory.getLogger(CloudWatchLogsService.class);

    private final CloudWatchLogsClient client;
    private final String logGroupName;
    private final boolean enabled;
    private final int maxEvents;
    private final int windowMinutes;

    public CloudWatchLogsService(
            CloudWatchLogsClient client,
            @Value("${incidentcommand.cloudwatch.log-group:}") String logGroupName,
            @Value("${incidentcommand.cloudwatch.enabled:false}") boolean enabled,
            @Value("${incidentcommand.cloudwatch.max-events:100}") int maxEvents,
            @Value("${incidentcommand.cloudwatch.window-minutes:15}") int windowMinutes) {
        this.client = client;
        this.logGroupName = logGroupName;
        this.enabled = enabled;
        this.maxEvents = maxEvents;
        this.windowMinutes = windowMinutes;
    }

    /** Most recent log lines first. Empty (not an error) if disabled or unreachable. */
    public List<LogLineDto> recentSimulatorLogs() {
        if (!enabled) {
            log.debug("CloudWatch logs disabled (incidentcommand.cloudwatch.enabled=false)");
            return List.of();
        }
        try {
            long startTimeMillis = Instant.now().minus(windowMinutes, ChronoUnit.MINUTES).toEpochMilli();
            FilterLogEventsRequest request = FilterLogEventsRequest.builder()
                    .logGroupName(logGroupName)
                    .startTime(startTimeMillis)
                    .limit(maxEvents)
                    .build();
            return client.filterLogEvents(request).events().stream()
                    .map(e -> new LogLineDto(e.timestamp(), e.message()))
                    .sorted(Comparator.comparing(LogLineDto::timestampMillis).reversed())
                    .toList();
        } catch (SdkException e) {
            log.warn("CloudWatch Logs read failed for group '{}': {} -- returning empty", logGroupName, e.toString());
            return List.of();
        }
    }
}
