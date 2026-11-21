package com.incidentcommand.backend.identity;

import java.util.List;

public record UserDto(Long id, String email, String displayName, List<String> roles, List<String> teams) {
    static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRoles().stream().map(Role::getName).toList(),
                user.getTeams().stream().map(Team::getName).toList());
    }
}
