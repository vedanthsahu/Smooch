package com.incidentcommand.backend.identity;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Level 1: no auth/RBAC enforcement yet -- Google SSO is blocked pending an
 * OAuth client (see docs/20-open-questions-and-risks.md). These endpoints
 * are open for now so the domain model can be built and tested; permission
 * checks land once real auth exists.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamRepository teamRepository;

    public TeamController(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    @GetMapping
    public List<TeamDto> list() {
        return teamRepository.findAll().stream().map(TeamDto::from).toList();
    }

    @GetMapping("/{id}")
    public TeamDto get(@PathVariable Long id) {
        return teamRepository.findById(id)
                .map(TeamDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "team not found"));
    }

    @PostMapping
    public ResponseEntity<TeamDto> create(@Valid @RequestBody CreateTeamRequest request) {
        if (teamRepository.findByName(request.name()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "team already exists");
        }
        Team saved = teamRepository.save(new Team(request.name()));
        return ResponseEntity.status(HttpStatus.CREATED).body(TeamDto.from(saved));
    }
}
