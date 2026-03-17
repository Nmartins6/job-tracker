package dev.nicolas.JobTracker.application.usecases.stage.start;

import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.StartStageRequest;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class StartStageUseCase {

    private final StageRepository stageRepository;

    public StartStageUseCase(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public StageResponse execute(UUID id, StartStageRequest request) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + id));

        stage.start(request.startedAt());
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
