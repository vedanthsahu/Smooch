package com.incidentcommand.backend.identity;

import jakarta.validation.constraints.NotBlank;

public record CreateTeamRequest(@NotBlank String name) {
}
