package com.incidentcommand.backend.incident;

import java.time.Instant;

import com.incidentcommand.backend.production.ProductionService;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ProductionService service;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private IncidentCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sla_policy_id", nullable = false)
    private SlaPolicy slaPolicy;

    @Column(name = "sla_deadline_at", nullable = false)
    private Instant slaDeadlineAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    protected Incident() {
    }

    public Incident(ProductionService service, IncidentCategory category, Severity severity, SlaPolicy slaPolicy) {
        this.service = service;
        this.category = category;
        this.severity = severity;
        this.slaPolicy = slaPolicy;
        this.status = IncidentStatus.CREATED;
        this.slaDeadlineAt = Instant.now().plusSeconds(slaPolicy.getSlaMinutes() * 60L);
    }

    public Long getId() {
        return id;
    }

    public ProductionService getService() {
        return service;
    }

    public IncidentCategory getCategory() {
        return category;
    }

    public Severity getSeverity() {
        return severity;
    }

    public IncidentStatus getStatus() {
        return status;
    }

    public void setStatus(IncidentStatus status) {
        this.status = status;
    }

    public SlaPolicy getSlaPolicy() {
        return slaPolicy;
    }

    public Instant getSlaDeadlineAt() {
        return slaDeadlineAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }
}
