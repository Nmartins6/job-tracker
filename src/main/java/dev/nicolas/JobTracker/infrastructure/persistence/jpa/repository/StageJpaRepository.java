package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.StageJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StageJpaRepository extends JpaRepository<StageJpaEntity, UUID> {

    List<StageJpaEntity> findByApplicationIdOrderByOrderIndexAsc(UUID applicationId);
}
