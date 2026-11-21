package com.incidentcommand.backend.incident;

import java.time.Instant;

public record IncidentDto(
        Long id,
        Long serviceId,
        String serviceName,
        String productionName,
        String category,
        String severity,
        String status,
        Instant slaDeadlineAt,
        Instant createdAt,
        Instant resolvedAt) {

    static IncidentDto from(Incident incident) {
        return new IncidentDto(
                incident.getId(),
                incident.getService().getId(),
                incident.getService().getName(),
                incident.getService().getProduction().getName(),
                incident.getCategory().getName(),
                incident.getSeverity().name(),
                incident.getStatus().name(),
                incident.getSlaDeadlineAt(),
                incident.getCreatedAt(),
                incident.getResolvedAt());
    }
}
