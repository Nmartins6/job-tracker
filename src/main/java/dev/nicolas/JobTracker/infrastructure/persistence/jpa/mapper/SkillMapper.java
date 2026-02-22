package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.skill.Skill;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.SkillJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SkillMapper {

    public SkillJpaEntity toJpaEntity(Skill skill) {
        return SkillJpaEntity.builder()
                .id(skill.getId())
                .name(skill.getName())
                .category(skill.getCategory())
                .build();
    }

    public Skill toDomain(SkillJpaEntity entity) {
        return Skill.reconstitute(
                entity.getId(),
                entity.getName(),
                entity.getCategory()
        );
    }

}
