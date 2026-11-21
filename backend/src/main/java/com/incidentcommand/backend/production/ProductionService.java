package com.incidentcommand.backend.production;

import com.incidentcommand.backend.identity.Team;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A service within a production (e.g. "payment-api"), owned by exactly one
 * team. Ownership is modeled at the service level, not the production level
 * -- see docs/23-incident-lifecycle-and-sla.md "Ownership model" for why.
 */
@Entity
@Table(name = "services")
public class ProductionService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_id", nullable = false)
    private ProductionEnvironment production;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owning_team_id", nullable = false)
    private Team owningTeam;

    protected ProductionService() {
    }

    public ProductionService(String name, ProductionEnvironment production, Team owningTeam) {
        this.name = name;
        this.production = production;
        this.owningTeam = owningTeam;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProductionEnvironment getProduction() {
        return production;
    }

    public Team getOwningTeam() {
        return owningTeam;
    }

    public void setOwningTeam(Team owningTeam) {
        this.owningTeam = owningTeam;
    }
}
