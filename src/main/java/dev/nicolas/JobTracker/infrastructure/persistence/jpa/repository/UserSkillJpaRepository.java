package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.UserSkillJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillJpaRepository extends JpaRepository<UserSkillJpaEntity, UUID> {

    List<UserSkillJpaEntity> findByUserId(UUID userId);

    Optional<UserSkillJpaEntity> findByUserIdAndSkillId(UUID userId, UUID skillId);
}
