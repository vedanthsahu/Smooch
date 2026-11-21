package com.incidentcommand.backend.incident;

import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.incidentcommand.backend.production.ProductionService;
import com.incidentcommand.backend.production.ProductionServiceRepository;
import com.incidentcommand.backend.simulator.SimulatorClient;

/**
 * Deterministic, threshold-based detection -- NO model call in this path.
 * See docs/09-ai-engine-architecture.md invariant and docs/23-incident-lifecycle-and-sla.md.
 *
 * Deliberately NOT @Transactional at this level: DB writes go through
 * IncidentLifecycleService (its own short transactions), and triggering an
 * investigation involves a multi-second HTTP+model call that must never run
 * inside a DB transaction -- see InvestigationService.
 *
 * Simplification vs. the full spec: resolution fires on the first healthy
 * check rather than N consecutive ones (docs/23 calls for a verification
 * window) -- fine for Level 2/3, revisit once this is noisy in practice.
 */
@Service
public class DetectionService {

    private final ProductionServiceRepository serviceRepository;
    private final IncidentLifecycleService incidentLifecycleService;
    private final InvestigationService investigationService;
    private final IncidentReportExportService reportExportService;
    private final SimulatorClient simulatorClient;

    public DetectionService(
            ProductionServiceRepository serviceRepository,
            IncidentLifecycleService incidentLifecycleService,
            InvestigationService investigationService,
            IncidentReportExportService reportExportService,
            SimulatorClient simulatorClient) {
        this.serviceRepository = serviceRepository;
        this.incidentLifecycleService = incidentLifecycleService;
        this.investigationService = investigationService;
        this.reportExportService = reportExportService;
        this.simulatorClient = simulatorClient;
    }

    @Scheduled(fixedDelayString = "${incidentcommand.detection.poll-interval-ms:5000}")
    public void pollAndDetect() {
        List<ProductionService> services = serviceRepository.findAll();
        for (ProductionService service : services) {
            checkService(service);
        }
    }

    private void checkService(ProductionService service) {
        Optional<SimulatorClient.ServiceHealth> health = simulatorClient.getHealth(service.getName());
        if (health.isEmpty()) {
            return; // simulator unreachable or service unknown to it -- can't assess, not "healthy"
        }

        boolean isHealthy = "healthy".equals(health.get().status());

        if (!isHealthy) {
            Optional<Long> newIncidentId = incidentLifecycleService.openIfNeeded(service, health.get().status());
            newIncidentId.ifPresent(investigationService::investigateAndPersist);
        } else {
            Optional<Long> resolvedIncidentId = incidentLifecycleService.resolveIfOpen(service);
            resolvedIncidentId.ifPresent(reportExportService::exportAndUpload);
        }
    }
}
