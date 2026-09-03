package com.incidentcommand.backend.production;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateServiceRequest(@NotBlank String name, @NotNull Long productionId, @NotNull Long owningTeamId) {
}
