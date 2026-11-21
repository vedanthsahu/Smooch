package com.incidentcommand.backend.incident;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.incidentcommand.backend.aiengine.AiEngineClient;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/**
 * The DB-writing half of investigation handling, on its own bean so
 * @Transactional actually applies -- Spring's proxy-based AOP does not
 * intercept self-invoked (this.method()) calls within the same class, which
 * is why this isn't just three methods on InvestigationService.
 */
@Service
public class InvestigationPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(InvestigationPersistenceService.class);

    private final IncidentRepository incidentRepository;
    private final InvestigationRepository investigationRepository;
    private final ObjectMapper objectMapper;

    public InvestigationPersistenceService(
            IncidentRepository incidentRepository,
            InvestigationRepository investigationRepository,
            ObjectMapper objectMapper) {
        this.incidentRepository = incidentRepository;
        this.investigationRepository = investigationRepository;
        this.objectMapper = objectMapper;
    }

    /** Returns the service name to investigate, or null if the incident no longer exists. */
    @Transactional
    public String beginInvestigation(Long incidentId) {
        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (incident == null) {
            return null;
        }
        incident.setStatus(IncidentStatus.INVESTIGATING);
        incidentRepository.save(incident);
        investigationRepository.save(new Investigation(incident));
        return incident.getService().getName();
    }

    @Transactional
    public void completeInvestigation(Long incidentId, AiEngineClient.InvestigationResult result) {
        Investigation investigation = investigationRepository.findByIncidentId(incidentId).orElse(null);
        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (investigation == null || incident == null) {
            return;
        }

        investigation.complete(
                result.diagnosis(), toJson(result.evidence()), String.join(",", result.servicesExamined()),
                result.model(), result.provider(), result.tier(), result.totalLatencyMs());
        investigationRepository.save(investigation);

        incident.setStatus(IncidentStatus.DIAGNOSED);
        incidentRepository.save(incident);

        log.info("Investigation for incident #{} completed via {}/{} ({} ms)",
                incidentId, result.provider(), result.model(), result.totalLatencyMs());
    }

    @Transactional
    public void failInvestigation(Long incidentId, String reason) {
        Investigation investigation = investigationRepository.findByIncidentId(incidentId).orElse(null);
        Incident incident = incidentRepository.findById(incidentId).orElse(null);
        if (investigation == null || incident == null) {
            return;
        }
        investigation.fail(reason);
        investigationRepository.save(investigation);

        // No diagnosis available -- escalate rather than leave it silently stuck.
        incident.setStatus(IncidentStatus.ESCALATED_TO_HUMAN);
        incidentRepository.save(incident);

        log.warn("Investigation for incident #{} failed: {} -- escalated to human", incidentId, reason);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JacksonException e) {
            return "[]";
        }
    }
}
