package com.incidentcommand.backend.incident;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlaPolicyRepository extends JpaRepository<SlaPolicy, Long> {
    Optional<SlaPolicy> findBySeverity(Severity severity);
}
