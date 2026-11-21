package com.incidentcommand.backend.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Thin wrapper over S3Client -- see docs/08-data-architecture.md "S3 vs.
 * PostgreSQL boundary". S3 is for artifacts (exported reports, archives),
 * never the system of record.
 *
 * Failures are logged and swallowed, not thrown: an artifact export is a
 * nice-to-have side effect, and must never break the core incident
 * lifecycle it's attached to (see IncidentReportExportService). This is
 * also deliberately tolerant of AWS not being configured yet -- see
 * docs/20-open-questions-and-risks.md.
 */
@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucket;
    private final boolean enabled;

    public S3StorageService(
            S3Client s3Client,
            @Value("${incidentcommand.s3.bucket:}") String bucket,
            @Value("${incidentcommand.s3.enabled:false}") boolean enabled) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.enabled = enabled;
    }

    /** Returns true if the upload actually happened. */
    public boolean putJson(String key, String jsonContent) {
        if (!enabled) {
            log.debug("S3 disabled (incidentcommand.s3.enabled=false) -- skipping upload of '{}'", key);
            return false;
        }
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType("application/json")
                    .build();
            s3Client.putObject(request, RequestBody.fromString(jsonContent));
            log.info("Uploaded s3://{}/{}", bucket, key);
            return true;
        } catch (SdkException e) {
            log.warn("S3 upload failed for key '{}': {} -- continuing without it", key, e.toString());
            return false;
        }
    }
}
