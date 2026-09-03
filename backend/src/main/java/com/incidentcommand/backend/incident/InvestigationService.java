package com.incidentcommand.backend.incident;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.incidentcommand.backend.aiengine.AiEngineClient;

/**
 * Orchestrator only -- no @Transactional here on purpose. Calls the
 * ai-engine (slow, seconds-scale HTTP+model call) with no DB transaction
 * held open, then delegates persistence to InvestigationPersistenceService.
 */
@Service
public class InvestigationService {

    private final AiEngineClient aiEngineClient;
    private final InvestigationPersistenceService persistence;

    public InvestigationService(AiEngineClient aiEngineClient, InvestigationPersistenceService persistence) {
        this.aiEngineClient = aiEngineClient;
        this.persistence = persistence;
    }

    public void investigateAndPersist(Long incidentId) {
        String serviceName = persistence.beginInvestigation(incidentId);
        if (serviceName == null) {
            return;
        }

        Optional<AiEngineClient.InvestigationResult> result = aiEngineClient.investigate(serviceName);

        if (result.isPresent()) {
            persistence.completeInvestigation(incidentId, result.get());
        } else {
            persistence.failInvestigation(incidentId, "ai-engine unreachable");
        }
    }
}
