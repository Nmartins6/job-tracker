package dev.nicolas.JobTracker.domain.skill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface SkillRepository {

    Skill save(Skill skill);

    Optional<Skill> findById(UUID id);

    List<Skill> findAll();

    Optional<Skill> findByName(String name);

    boolean existsByName(String name);
}
