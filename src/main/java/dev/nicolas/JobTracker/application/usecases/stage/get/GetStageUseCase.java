package dev.nicolas.JobTracker.application.usecases.stage.get;

import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetStageUseCase {

    private final StageRepository stageRepository;

    public GetStageUseCase(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    public StageResponse findById(UUID id) {
        Stage stage = stageRepository.findById(id)
                .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + id));

        return toResponse(stage);
    }

    public List<StageResponse> findByApplicationId(UUID applicationId) {
        return stageRepository.findByApplicationId(applicationId).stream()
                .map(this::toResponse)
                .toList();
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
