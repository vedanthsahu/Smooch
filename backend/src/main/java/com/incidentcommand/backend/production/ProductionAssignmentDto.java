package com.incidentcommand.backend.production;

public record ProductionAssignmentDto(
        Long id,
        Long userId,
        String userEmail,
        Long productionId,
        String productionName,
        String role) {

    static ProductionAssignmentDto from(ProductionAssignment assignment) {
        return new ProductionAssignmentDto(
                assignment.getId(),
                assignment.getUser().getId(),
                assignment.getUser().getEmail(),
                assignment.getProduction().getId(),
                assignment.getProduction().getName(),
                assignment.getRole().name());
    }
}
