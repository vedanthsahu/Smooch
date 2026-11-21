package com.incidentcommand.backend.production;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.incidentcommand.backend.identity.Team;
import com.incidentcommand.backend.identity.TeamRepository;

/**
 * Seeds the production/service topology the demo's only implemented scenario
 * (simulator/app/main.py Scenario A) actually targets -- "payment-api" and
 * "payment-worker" under "payments" -- so DetectionService's poll loop has
 * something to check out of the box. Without this, the demo UI's "Start demo
 * incident" button injects a real failure into the simulator, but no incident
 * is ever created because ProductionServiceRepository.findAll() stays empty
 * (caught while verifying the new frontend end-to-end: the button worked, the
 * simulator genuinely degraded, but nothing ever showed up in the incident
 * list).
 *
 * Illustrative data, same spirit as IncidentSeeder's SLA policies -- not
 * meant to represent a real org chart.
 */
@Component
@Order(20)
public class ProductionDemoSeeder implements CommandLineRunner {

    private final ProductionEnvironmentRepository productionRepository;
    private final ProductionServiceRepository serviceRepository;
    private final TeamRepository teamRepository;

    public ProductionDemoSeeder(
            ProductionEnvironmentRepository productionRepository,
            ProductionServiceRepository serviceRepository,
            TeamRepository teamRepository) {
        this.productionRepository = productionRepository;
        this.serviceRepository = serviceRepository;
        this.teamRepository = teamRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        ProductionEnvironment payments = productionRepository.findByName("payments")
                .orElseGet(() -> productionRepository.save(new ProductionEnvironment("payments")));

        Team paymentsTeam = teamRepository.findByName("Payments Team")
                .orElseGet(() -> teamRepository.save(new Team("Payments Team")));

        ensureService("payment-api", payments, paymentsTeam);
        ensureService("payment-worker", payments, paymentsTeam);
    }

    private void ensureService(String name, ProductionEnvironment production, Team owningTeam) {
        boolean exists = serviceRepository.findByProductionId(production.getId()).stream()
                .anyMatch(existing -> existing.getName().equals(name));
        if (!exists) {
            serviceRepository.save(new ProductionService(name, production, owningTeam));
        }
    }
}
