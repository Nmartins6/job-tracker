package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.ApplicationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class ApplicationMapper {

    public ApplicationJpaEntity toJpaEntity(Application application) {
        return ApplicationJpaEntity.builder()
                .id(application.getId())
                .userId(application.getUserId())
                .jobId(application.getJobId())
                .status(application.getStatus())
                .nextAction(application.getNextAction())
                .nextActionDueAt(application.getNextActionDueAt())
                .build();
    }

    public Application toDomain(ApplicationJpaEntity entity) {
        return Application.reconstitute(
                entity.getId(),
                entity.getUserId(),
                entity.getJobId(),
                entity.getStatus(),
                entity.getNextAction(),
                entity.getNextActionDueAt()
        );
    }
}
