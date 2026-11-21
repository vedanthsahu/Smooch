package com.incidentcommand.backend.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** Illustrative seed values (P1=30min, P2=90min, P3=240min) -- see docs/23-incident-lifecycle-and-sla.md. */
@Entity
@Table(name = "sla_policies")
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Severity severity;

    @Column(name = "sla_minutes", nullable = false)
    private int slaMinutes;

    protected SlaPolicy() {
    }

    public SlaPolicy(Severity severity, int slaMinutes) {
        this.severity = severity;
        this.slaMinutes = slaMinutes;
    }

    public Long getId() {
        return id;
    }

    public Severity getSeverity() {
        return severity;
    }

    public int getSlaMinutes() {
        return slaMinutes;
    }
}
