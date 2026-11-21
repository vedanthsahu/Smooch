package com.incidentcommand.backend.incident;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestigationRepository extends JpaRepository<Investigation, Long> {
    Optional<Investigation> findByIncidentId(Long incidentId);
}
