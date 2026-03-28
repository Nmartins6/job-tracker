package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.NoteJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NoteJpaRepository extends JpaRepository<NoteJpaEntity, UUID> {

    List<NoteJpaEntity> findByApplicationIdOrderByCreatedAtAsc(UUID applicationId);

    boolean existsByStageId(UUID stageId);
}
