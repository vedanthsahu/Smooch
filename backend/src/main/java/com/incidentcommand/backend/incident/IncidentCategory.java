package com.incidentcommand.backend.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** See docs/23-incident-lifecycle-and-sla.md severity/SLA table (illustrative seed values). */
@Entity
@Table(name = "incident_categories")
public class IncidentCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_severity", nullable = false)
    private Severity defaultSeverity;

    protected IncidentCategory() {
    }

    public IncidentCategory(String name, Severity defaultSeverity) {
        this.name = name;
        this.defaultSeverity = defaultSeverity;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Severity getDefaultSeverity() {
        return defaultSeverity;
    }
}
