package com.incidentcommand.backend.production;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionAssignmentRepository extends JpaRepository<ProductionAssignment, Long> {
    List<ProductionAssignment> findByUserId(Long userId);

    List<ProductionAssignment> findByProductionId(Long productionId);
}
