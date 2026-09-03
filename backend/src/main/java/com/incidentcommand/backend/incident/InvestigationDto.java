package com.incidentcommand.backend.incident;

import java.time.Instant;

public record InvestigationDto(
        String status,
        String diagnosis,
        String evidenceJson,
        String servicesExamined,
        String model,
        String provider,
        String tier,
        Integer totalLatencyMs,
        Instant startedAt,
        Instant completedAt) {

    static InvestigationDto from(Investigation investigation) {
        return new InvestigationDto(
                investigation.getStatus().name(),
                investigation.getDiagnosis(),
                investigation.getEvidenceJson(),
                investigation.getServicesExamined(),
                investigation.getModel(),
                investigation.getProvider(),
                investigation.getTier(),
                investigation.getTotalLatencyMs(),
                investigation.getStartedAt(),
                investigation.getCompletedAt());
    }
}
