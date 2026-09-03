package com.incidentcommand.backend.incident;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Level 2/3: no RBAC scoping yet (production-assignment-based filtering lands
 * once real auth exists -- see docs/15-security-and-rbac.md). Every incident
 * is visible to every caller for now.
 */
@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final InvestigationRepository investigationRepository;

    public IncidentController(
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            InvestigationRepository investigationRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.investigationRepository = investigationRepository;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<IncidentDto> list() {
        return incidentRepository.findAll().stream().map(IncidentDto::from).toList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public IncidentDetailDto get(@PathVariable Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "incident not found"));
        List<IncidentEventDto> timeline = incidentEventRepository.findByIncidentIdOrderByCreatedAtAsc(id).stream()
                .map(IncidentEventDto::from)
                .toList();
        InvestigationDto investigation = investigationRepository.findByIncidentId(id)
                .map(InvestigationDto::from)
                .orElse(null);
        return new IncidentDetailDto(IncidentDto.from(incident), timeline, investigation);
    }
}
