package dev.nicolas.JobTracker.application.usecases.stage.update;

import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.UpdateStageRequest;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateStageUseCase {

    private final StageRepository stageRepository;

    public UpdateStageUseCase(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public StageResponse execute(UUID id, UpdateStageRequest request) {
        Stage existing = stageRepository.findById(id)
                .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + id));

        Stage validated = Stage.create(
                existing.getApplicationId(),
                request.name(),
                request.orderIndex(),
                request.deadlineAt()
        );

        Stage saved = stageRepository.save(Stage.reconstitute(
                existing.getId(),
                existing.getApplicationId(),
                validated.getName(),
                validated.getOrderIndex(),
                existing.getStartedAt(),
                existing.getCompletedAt(),
                validated.getDeadlineAt()
        ));

        return new StageResponse(
                saved.getId(),
                saved.getApplicationId(),
                saved.getName(),
                saved.getOrderIndex(),
                saved.getStartedAt(),
                saved.getCompletedAt(),
                saved.getDeadlineAt()
        );
    }
}
