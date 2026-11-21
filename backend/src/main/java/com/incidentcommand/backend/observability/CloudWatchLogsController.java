package com.incidentcommand.backend.observability;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Demo-only read surface for the Production Simulator's real CloudWatch
 * Logs -- see CloudWatchLogsService. No RBAC scoping yet, same as
 * IncidentController at this level of the project.
 */
@RestController
@RequestMapping("/api/logs")
public class CloudWatchLogsController {

    private final CloudWatchLogsService logsService;

    public CloudWatchLogsController(CloudWatchLogsService logsService) {
        this.logsService = logsService;
    }

    @GetMapping("/simulator")
    public List<LogLineDto> simulatorLogs() {
        return logsService.recentSimulatorLogs();
    }
}
