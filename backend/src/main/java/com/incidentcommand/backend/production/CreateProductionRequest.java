package com.incidentcommand.backend.production;

import jakarta.validation.constraints.NotBlank;

public record CreateProductionRequest(@NotBlank String name) {
}
