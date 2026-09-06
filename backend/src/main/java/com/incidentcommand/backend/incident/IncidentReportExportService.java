package com.incidentcommand.backend.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.incidentcommand.backend.storage.S3StorageService;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * Exports a resolved incident's full record (incident + timeline +
 * investigation) as a JSON artifact to S3 -- see docs/19-aws-strategy.md "S3"
 * and docs/08-data-architecture.md "S3 vs. PostgreSQL boundary": this is
 * exactly the "exported diagnostic report" use case, not a replacement for
 * Postgres as the system of record (the DB row is still authoritative --
 * this is a convenience export, not a required part of the incident
 * lifecycle -- see S3StorageService's fail-soft behavior).
 *
 * Deliberately no @Transactional here: the DTO read (which touches lazy
 * associations) happens in IncidentReportDataProvider's own transaction;
 * this method only runs after that transaction has closed, so the S3 upload
 * (network I/O) never holds a DB connection open.
 */
@Service
public class IncidentReportExportService {

    private static final Logger log = LoggerFactory.getLogger(IncidentReportExportService.class);

    private final IncidentReportDataProvider dataProvider;
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;

    public IncidentReportExportService(
            IncidentReportDataProvider dataProvider,
            S3StorageService s3StorageService,
            ObjectMapper objectMapper) {
        this.dataProvider = dataProvider;
        this.s3StorageService = s3StorageService;
        this.objectMapper = objectMapper;
    }

    /** Never throws -- a failed export must not affect the incident lifecycle
     * or the detection loop that triggers it (see DetectionService). */
    public void exportAndUpload(Long incidentId) {
        dataProvider.buildReport(incidentId).ifPresent(report -> {
            try {
                String json = objectMapper.writeValueAsString(report);
                String key = "incident-reports/%d.json".formatted(incidentId);
                s3StorageService.putJson(key, json);
            } catch (JacksonException e) {
                log.warn("Failed to serialize report for incident #{}: {} -- skipping export", incidentId, e.toString());
            }
        });
    }
}
