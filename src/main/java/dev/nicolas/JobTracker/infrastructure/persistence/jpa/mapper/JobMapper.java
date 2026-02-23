package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobJpaEntity toJpaEntity(Job job) {
        return JobJpaEntity.builder()
                .id(job.getId())
                .company(job.getCompany())
                .title(job.getTitle())
                .sourceUrl(job.getSourceUrl())
                .seniority(job.getSeniority())
                .location(job.getLocation())
                .description(job.getDescription())
                .build();
    }

    public Job toDomain(JobJpaEntity entity) {
        return Job.reconstitute(
                entity.getId(),
                entity.getCompany(),
                entity.getTitle(),
                entity.getSourceUrl(),
                entity.getSeniority(),
                entity.getLocation(),
                entity.getDescription()
        );
    }

}
