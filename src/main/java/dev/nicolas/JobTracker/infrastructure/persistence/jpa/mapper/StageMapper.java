package dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper;

import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.StageJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class StageMapper {

    public StageJpaEntity toJpaEntity(Stage stage) {
        return StageJpaEntity.builder()
                .id(stage.getId())
                .applicationId(stage.getApplicationId())
                .name(stage.getName())
                .orderIndex(stage.getOrderIndex())
                .startedAt(stage.getStartedAt())
                .completedAt(stage.getCompletedAt())
                .deadlineAt(stage.getDeadlineAt())
                .build();
    }

    public Stage toDomain(StageJpaEntity entity) {
        return Stage.reconstitute(
                entity.getId(),
                entity.getApplicationId(),
                entity.getName(),
                entity.getOrderIndex(),
                entity.getStartedAt(),
                entity.getCompletedAt(),
                entity.getDeadlineAt()
        );
    }
}
