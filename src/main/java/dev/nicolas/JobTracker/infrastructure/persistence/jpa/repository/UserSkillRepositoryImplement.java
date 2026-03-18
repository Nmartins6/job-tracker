package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.UserSkillJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.UserSkillMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserSkillRepositoryImplement implements UserSkillRepository {

    private final UserSkillJpaRepository userSkillJpaRepository;
    private final UserSkillMapper mapper;

    public UserSkillRepositoryImplement(UserSkillJpaRepository userSkillJpaRepository, UserSkillMapper mapper) {
        this.userSkillJpaRepository = userSkillJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public UserSkill save(UserSkill userSkill) {
        UserSkillJpaEntity entity = mapper.toJpaEntity(userSkill);
        UserSkillJpaEntity saved = userSkillJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<UserSkill> findById(UUID id) {
        return userSkillJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<UserSkill> findByUserId(UUID userId) {
        return userSkillJpaRepository.findByUserId(userId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId) {
        return userSkillJpaRepository.findByUserIdAndSkillId(userId, skillId).map(mapper::toDomain);
    }
}
