package com.incidentcommand.backend.production;

import jakarta.validation.constraints.NotNull;

public record CreateAssignmentRequest(@NotNull Long userId, @NotNull Long productionId, @NotNull ProductionRole role) {
}
