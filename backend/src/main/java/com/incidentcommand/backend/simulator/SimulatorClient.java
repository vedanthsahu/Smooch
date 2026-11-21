package com.incidentcommand.backend.simulator;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SimulatorClient {

    public record ServiceHealth(String serviceId, String status, String production) {
    }

    /** See simulator/app/main.py ScenarioStartResponse -- field names are the
     * simulator's own snake_case JSON, not renamed by a global Jackson strategy
     * here (matches the explicit-per-field convention in AiEngineClient). */
    public record ScenarioStartResponse(
            String scenario,
            @JsonProperty("target_service_id") String targetServiceId,
            @JsonProperty("started_at") double startedAt) {
    }

    private final RestClient simulatorRestClient;

    public SimulatorClient(RestClient simulatorRestClient) {
        this.simulatorRestClient = simulatorRestClient;
    }

    /** Empty if the simulator is unreachable or the service isn't known to it --
     * detection treats that as "can't assess," not "healthy." */
    public Optional<ServiceHealth> getHealth(String serviceId) {
        try {
            ServiceHealth health = simulatorRestClient.get()
                    .uri("/services/{id}/health", serviceId)
                    .retrieve()
                    .body(ServiceHealth.class);
            return Optional.ofNullable(health);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    /** Starts Scenario A (queue consumer failure, see docs/22-production-simulator.md) --
     * the only scenario implemented so far. DetectionService's existing 5s poll picks up
     * the resulting degradation on its own; this just injects the failure. */
    public Optional<ScenarioStartResponse> startQueueConsumerFailureScenario() {
        try {
            ScenarioStartResponse response = simulatorRestClient.post()
                    .uri("/scenarios/queue-consumer-failure/start")
                    .retrieve()
                    .body(ScenarioStartResponse.class);
            return Optional.ofNullable(response);
        } catch (RestClientException e) {
            return Optional.empty();
        }
    }

    /** Clears whatever scenario is active, letting the next poll observe healthy again. */
    public boolean resetScenario() {
        try {
            simulatorRestClient.post().uri("/scenarios/reset").retrieve().toBodilessEntity();
            return true;
        } catch (RestClientException e) {
            return false;
        }
    }
}
