package com.incidentcommand.backend.simulator;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lets the demo UI drive the Production Simulator without a direct
 * frontend-to-simulator call (no CORS setup on the simulator itself, and
 * keeps a single API surface for the frontend to talk to). Proxies straight
 * through SimulatorClient -- no incident logic here, DetectionService's
 * existing poll loop is what actually notices the resulting degradation.
 */
@RestController
@RequestMapping("/api/simulator")
public class SimulatorController {

    private final SimulatorClient simulatorClient;

    public SimulatorController(SimulatorClient simulatorClient) {
        this.simulatorClient = simulatorClient;
    }

    @PostMapping("/scenarios/queue-consumer-failure/start")
    public ResponseEntity<ScenarioStartedDto> startQueueConsumerFailure() {
        return simulatorClient.startQueueConsumerFailureScenario()
                .map(ScenarioStartedDto::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.BAD_GATEWAY).build());
    }

    @PostMapping("/scenarios/reset")
    public ResponseEntity<Void> resetScenario() {
        boolean ok = simulatorClient.resetScenario();
        return ok ? ResponseEntity.ok().build() : ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }
}
