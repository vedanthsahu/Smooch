package com.incidentcommand.backend.incident;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Illustrative seed values -- see docs/23-incident-lifecycle-and-sla.md "Severity and SLA". */
@Component
@Order(10)
public class IncidentSeeder implements CommandLineRunner {

    private final IncidentCategoryRepository categoryRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    public IncidentSeeder(IncidentCategoryRepository categoryRepository, SlaPolicyRepository slaPolicyRepository) {
        this.categoryRepository = categoryRepository;
        this.slaPolicyRepository = slaPolicyRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        slaPolicyRepository.findBySeverity(Severity.P1)
                .orElseGet(() -> slaPolicyRepository.save(new SlaPolicy(Severity.P1, 30)));
        slaPolicyRepository.findBySeverity(Severity.P2)
                .orElseGet(() -> slaPolicyRepository.save(new SlaPolicy(Severity.P2, 90)));
        slaPolicyRepository.findBySeverity(Severity.P3)
                .orElseGet(() -> slaPolicyRepository.save(new SlaPolicy(Severity.P3, 240)));

        categoryRepository.findByName("service-degraded")
                .orElseGet(() -> categoryRepository.save(new IncidentCategory("service-degraded", Severity.P1)));
    }
}
