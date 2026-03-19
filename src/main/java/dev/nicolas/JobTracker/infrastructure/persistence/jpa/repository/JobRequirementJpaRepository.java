package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobRequirementJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRequirementJpaRepository extends JpaRepository<JobRequirementJpaEntity, UUID> {

    List<JobRequirementJpaEntity> findByJobId(UUID jobId);

    Optional<JobRequirementJpaEntity> findByJobIdAndSkillId(UUID jobId, UUID skillId);
}
