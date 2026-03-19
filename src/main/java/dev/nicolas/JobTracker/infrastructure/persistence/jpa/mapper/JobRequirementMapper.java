package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobRequirementJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class JobRequirementMapper {

    public JobRequirementJpaEntity toJpaEntity(JobRequirement jobRequirement) {
        return JobRequirementJpaEntity.builder()
                .id(jobRequirement.getId())
                .jobId(jobRequirement.getJobId())
                .skillId(jobRequirement.getSkillId())
                .mustHave(jobRequirement.isMustHave())
                .desiredLevel(jobRequirement.getDesiredLevel())
                .weight(jobRequirement.getWeight())
                .build();
    }

    public JobRequirement toDomain(JobRequirementJpaEntity entity) {
        return JobRequirement.reconstitute(
                entity.getId(),
                entity.getJobId(),
                entity.getSkillId(),
                entity.isMustHave(),
                entity.getDesiredLevel(),
                entity.getWeight()
        );
    }
}
