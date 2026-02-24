package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobJpaRepository extends JpaRepository<JobJpaEntity, UUID> {
}
