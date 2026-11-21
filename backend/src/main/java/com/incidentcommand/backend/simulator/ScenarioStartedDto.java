package com.incidentcommand.backend.simulator;

/**
 * Deliberately separate from SimulatorClient.ScenarioStartResponse: that
 * record's @JsonProperty annotations exist to deserialize the simulator's own
 * snake_case JSON, but Jackson applies the same annotations on serialization
 * too -- returning it directly from the controller would leak snake_case
 * field names into this API's response, inconsistent with every other DTO
 * here (IncidentDto, InvestigationDto, ...), which are plain camelCase.
 */
public record ScenarioStartedDto(String scenario, String targetServiceId, double startedAt) {
    static ScenarioStartedDto from(SimulatorClient.ScenarioStartResponse response) {
        return new ScenarioStartedDto(response.scenario(), response.targetServiceId(), response.startedAt());
    }
}
