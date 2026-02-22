package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.SkillJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.SkillMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SkillRepositoryImplement implements SkillRepository {

    private final SkillJpaRepository skillJpaRepository;
    private final SkillMapper mapper;

    public SkillRepositoryImplement(SkillJpaRepository skillRepository, SkillMapper mapper) {
        this.skillJpaRepository = skillRepository;
        this.mapper = mapper;
    }

    @Override
    public Skill save(Skill skill) {
        SkillJpaEntity entity = mapper.toJpaEntity(skill);
        SkillJpaEntity saved = skillJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Skill> findById(UUID id) {
        return skillJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Skill> findAll() {
        return skillJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Skill> findByName(String name) {
        return skillJpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public boolean existsByName(String name) {
        return skillJpaRepository.existsByName(name);
    }
}
