package com.incidentcommand.backend.incident;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentEventRepository extends JpaRepository<IncidentEvent, Long> {
    List<IncidentEvent> findByIncidentIdOrderByCreatedAtAsc(Long incidentId);
}
