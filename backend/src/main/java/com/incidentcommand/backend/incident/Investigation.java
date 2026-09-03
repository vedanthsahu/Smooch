package com.incidentcommand.backend.incident;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Level 3: one investigation per incident, sequential evidence gathering +
 * one model call (see docs/09-ai-engine-architecture.md). Per-source
 * investigation_evidence rows (docs/08-data-architecture.md) are not
 * persisted yet -- the ai-engine's evidence list is stored as-is in
 * evidenceJson for now rather than normalized.
 */
@Entity
@Table(name = "investigations")
public class Investigation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, unique = true)
    private Incident incident;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvestigationStatus status;

    @Lob
    @Column(name = "diagnosis")
    private String diagnosis;

    @Lob
    @Column(name = "evidence_json")
    private String evidenceJson;

    @Column(name = "services_examined")
    private String servicesExamined;

    @Column(name = "model")
    private String model;

    @Column(name = "provider")
    private String provider;

    @Column(name = "tier")
    private String tier;

    @Column(name = "total_latency_ms")
    private Integer totalLatencyMs;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    protected Investigation() {
    }

    public Investigation(Incident incident) {
        this.incident = incident;
        this.status = InvestigationStatus.RUNNING;
    }

    public Long getId() {
        return id;
    }

    public Incident getIncident() {
        return incident;
    }

    public InvestigationStatus getStatus() {
        return status;
    }

    public void complete(String diagnosis, String evidenceJson, String servicesExamined, String model,
            String provider, String tier, Integer totalLatencyMs) {
        this.status = InvestigationStatus.COMPLETED;
        this.diagnosis = diagnosis;
        this.evidenceJson = evidenceJson;
        this.servicesExamined = servicesExamined;
        this.model = model;
        this.provider = provider;
        this.tier = tier;
        this.totalLatencyMs = totalLatencyMs;
        this.completedAt = Instant.now();
    }

    public void fail(String reason) {
        this.status = InvestigationStatus.FAILED;
        this.diagnosis = reason;
        this.completedAt = Instant.now();
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getEvidenceJson() {
        return evidenceJson;
    }

    public String getServicesExamined() {
        return servicesExamined;
    }

    public String getModel() {
        return model;
    }

    public String getProvider() {
        return provider;
    }

    public String getTier() {
        return tier;
    }

    public Integer getTotalLatencyMs() {
        return totalLatencyMs;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }
}
