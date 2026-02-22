package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.SkillJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillJpaRepository extends JpaRepository<SkillJpaEntity, UUID> {

    Optional<SkillJpaEntity> findByName(String name);

    boolean existsByName(String name);
}
