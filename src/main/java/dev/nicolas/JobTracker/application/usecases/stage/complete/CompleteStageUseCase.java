package dev.nicolas.JobTracker.application.usecases.stage.complete;

import dev.nicolas.JobTracker.application.dto.stage.CompleteStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CompleteStageUseCase {

    private final StageRepository stageRepository;

    public CompleteStageUseCase(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public StageResponse execute(UUID id, CompleteStageRequest request) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + id));

        stage.complete(request.completedAt());
        Stage saved = stageRepository.save(stage);

        return toResponse(saved);
    }

    private StageResponse toResponse(Stage stage) {
        return new StageResponse(
                stage.getId(),
                stage.getApplicationId(),
                stage.getName(),
                stage.getOrderIndex(),
                stage.getStartedAt(),
                stage.getCompletedAt(),
                stage.getDeadlineAt()
        );
    }
}
