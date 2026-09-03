package com.incidentcommand.backend.incident;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incidentcommand.backend.production.ProductionService;

/**
 * Pure DB writes for incident open/resolve -- deliberately separate from
 * DetectionService (the scheduled poller) and InvestigationService (the slow
 * HTTP call to the ai-engine), so a Spring @Transactional DB transaction is
 * never held open across a multi-second external call.
 */
@Service
public class IncidentLifecycleService {

    private static final Logger log = LoggerFactory.getLogger(IncidentLifecycleService.class);
    private static final String DEFAULT_CATEGORY = "service-degraded";

    private final IncidentRepository incidentRepository;
    private final IncidentEventRepository incidentEventRepository;
    private final IncidentCategoryRepository categoryRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    public IncidentLifecycleService(
            IncidentRepository incidentRepository,
            IncidentEventRepository incidentEventRepository,
            IncidentCategoryRepository categoryRepository,
            SlaPolicyRepository slaPolicyRepository) {
        this.incidentRepository = incidentRepository;
        this.incidentEventRepository = incidentEventRepository;
        this.categoryRepository = categoryRepository;
        this.slaPolicyRepository = slaPolicyRepository;
    }

    /** Returns the new incident's id if one was created (caller uses this to
     * decide whether to kick off an investigation), empty if one was already open. */
    @Transactional
    public Optional<Long> openIfNeeded(ProductionService service, String observedStatus) {
        if (incidentRepository.findOpenByServiceId(service.getId()).isPresent()) {
            return Optional.empty();
        }

        IncidentCategory category = categoryRepository.findByName(DEFAULT_CATEGORY)
                .orElseThrow(() -> new IllegalStateException("default incident category not seeded"));
        SlaPolicy slaPolicy = slaPolicyRepository.findBySeverity(category.getDefaultSeverity())
                .orElseThrow(() -> new IllegalStateException("SLA policy not seeded for " + category.getDefaultSeverity()));

        Incident incident = incidentRepository.save(
                new Incident(service, category, category.getDefaultSeverity(), slaPolicy));
        incidentEventRepository.save(new IncidentEvent(
                incident, "DETECTED", "system", "observed_status=" + observedStatus));

        log.info("Incident #{} created for service '{}' (status={}, SLA deadline={})",
                incident.getId(), service.getName(), observedStatus, incident.getSlaDeadlineAt());
        return Optional.of(incident.getId());
    }

    @Transactional
    public void resolveIfOpen(ProductionService service) {
        incidentRepository.findOpenByServiceId(service.getId()).ifPresent(incident -> {
            incident.setStatus(IncidentStatus.RESOLVED);
            incident.setResolvedAt(Instant.now());
            incidentRepository.save(incident);
            incidentEventRepository.save(new IncidentEvent(incident, "RESOLVED", "system", null));
            log.info("Incident #{} resolved for service '{}'", incident.getId(), service.getName());
        });
    }
}
