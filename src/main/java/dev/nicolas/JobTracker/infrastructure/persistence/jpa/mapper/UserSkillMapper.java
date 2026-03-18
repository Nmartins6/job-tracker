package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.UserSkillJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserSkillMapper {

    public UserSkillJpaEntity toJpaEntity(UserSkill userSkill) {
        return UserSkillJpaEntity.builder()
                .id(userSkill.getId())
                .userId(userSkill.getUserId())
                .skillId(userSkill.getSkillId())
                .yearsExperience(userSkill.getYearsExperience())
                .level(userSkill.getLevel())
                .build();
    }

    public UserSkill toDomain(UserSkillJpaEntity entity) {
        return UserSkill.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getSkillId(),
                entity.getYearsExperience(),
                entity.getLevel()
        );
    }
}
