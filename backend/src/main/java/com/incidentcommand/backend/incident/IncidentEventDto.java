package com.incidentcommand.backend.incident;

import java.time.Instant;

public record IncidentEventDto(Long id, String eventType, String actor, String payload, Instant createdAt) {
    static IncidentEventDto from(IncidentEvent event) {
        return new IncidentEventDto(event.getId(), event.getEventType(), event.getActor(), event.getPayload(), event.getCreatedAt());
    }
}
