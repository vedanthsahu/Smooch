package com.incidentcommand.backend.incident;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByServiceId(Long serviceId);

    @org.springframework.data.jpa.repository.Query(
            "select i from Incident i where i.service.id = :serviceId and i.status <> 'RESOLVED' and i.status <> 'CLOSED_SLA_BREACHED'")
    Optional<Incident> findOpenByServiceId(Long serviceId);

    List<Incident> findByStatusNotIn(List<IncidentStatus> statuses);
}
