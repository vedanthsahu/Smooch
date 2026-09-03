package com.incidentcommand.backend.aiengine;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ai-engine's Python/FastAPI JSON responses are snake_case; this API's
 * own DTOs are camelCase by convention (see docs), so the mapping is done
 * explicitly here via @JsonProperty rather than changing the app-wide
 * Jackson naming strategy, which would also affect this API's own responses.
 */
@Component
public class AiEngineClient {

    private static final Logger log = LoggerFactory.getLogger(AiEngineClient.class);

    public record EvidenceSource(
            @JsonProperty("service_id") String serviceId,
            @JsonProperty("source_type") String sourceType,
            Object content,
            @JsonProperty("latency_ms") int latencyMs,
            boolean available) {
    }

    public record InvestigationResult(
            @JsonProperty("service_id") String serviceId,
            @JsonProperty("services_examined") List<String> servicesExamined,
            String diagnosis,
            List<EvidenceSource> evidence,
            String model,
            String provider,
            String tier,
            @JsonProperty("total_latency_ms") int totalLatencyMs) {
    }

    private final RestClient aiEngineRestClient;

    public AiEngineClient(RestClient aiEngineRestClient) {
        this.aiEngineRestClient = aiEngineRestClient;
    }

    /** Empty if the ai-engine is unreachable -- caller should record a FAILED
     * investigation, never fabricate a diagnosis. */
    public Optional<InvestigationResult> investigate(String serviceId) {
        try {
            InvestigationResult result = aiEngineRestClient.get()
                    .uri("/investigate/{serviceId}", serviceId)
                    .retrieve()
                    .body(InvestigationResult.class);
            return Optional.ofNullable(result);
        } catch (RestClientException e) {
            log.warn("ai-engine investigate('{}') failed: {}", serviceId, e.toString());
            return Optional.empty();
        }
    }
}
