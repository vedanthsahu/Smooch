package com.incidentcommand.backend.production;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionServiceRepository extends JpaRepository<ProductionService, Long> {
    List<ProductionService> findByProductionId(Long productionId);
}
