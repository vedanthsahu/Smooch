package com.incidentcommand.backend.production;

import com.incidentcommand.backend.identity.User;

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
import jakarta.persistence.UniqueConstraint;

/** One user's role on one production -- the core of resource-ownership scoping. */
@Entity
@Table(name = "production_assignments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "production_id"}))
public class ProductionAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "production_id", nullable = false)
    private ProductionEnvironment production;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductionRole role;

    protected ProductionAssignment() {
    }

    public ProductionAssignment(User user, ProductionEnvironment production, ProductionRole role) {
        this.user = user;
        this.production = production;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ProductionEnvironment getProduction() {
        return production;
    }

    public ProductionRole getRole() {
        return role;
    }

    public void setRole(ProductionRole role) {
        this.role = role;
    }
}
