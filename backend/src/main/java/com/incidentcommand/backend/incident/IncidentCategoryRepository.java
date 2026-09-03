package com.incidentcommand.backend.incident;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentCategoryRepository extends JpaRepository<IncidentCategory, Long> {
    Optional<IncidentCategory> findByName(String name);
}
