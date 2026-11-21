package com.incidentcommand.backend.identity;

public record TeamDto(Long id, String name) {
    static TeamDto from(Team team) {
        return new TeamDto(team.getId(), team.getName());
    }
}
