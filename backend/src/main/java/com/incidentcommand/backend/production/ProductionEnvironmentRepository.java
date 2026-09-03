package com.incidentcommand.backend.production;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductionEnvironmentRepository extends JpaRepository<ProductionEnvironment, Long> {
    Optional<ProductionEnvironment> findByName(String name);
}
