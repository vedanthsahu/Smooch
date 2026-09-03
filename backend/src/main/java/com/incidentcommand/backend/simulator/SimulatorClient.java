package com.incidentcommand.backend.simulator;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class SimulatorClient {

    public record ServiceHealth(String serviceId, String status, String production) {
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
}
