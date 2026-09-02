package com.incidentcommand.backend.health;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Level 0 walking skeleton: proves the call path frontend -> backend -> ai-engine -> simulator.
 * No incident/RBAC logic yet -- see docs/03-scope-and-roadmap.md.
 */
@RestController
public class HealthController {

    private final RestClient aiEngineRestClient;

    public HealthController(RestClient aiEngineRestClient) {
        this.aiEngineRestClient = aiEngineRestClient;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "ok", "service", "backend");
    }

    @GetMapping("/api/health/deep")
    public Map<String, Object> healthDeep() {
        try {
            Map<?, ?> aiEngineHealth = aiEngineRestClient.get()
                    .uri("/health/deep")
                    .retrieve()
                    .body(Map.class);
            return Map.of("status", "ok", "service", "backend", "aiEngine", aiEngineHealth);
        } catch (RestClientException e) {
            return Map.of("status", "error", "service", "backend", "detail", "ai-engine unreachable: " + e.getMessage());
        }
    }
}
