package dev.nicolas.JobTracker.application.usecases.stage.create;

import dev.nicolas.JobTracker.application.dto.stage.CreateStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateStageUseCase {

    private final StageRepository stageRepository;
    private final ApplicationRepository applicationRepository;

    public CreateStageUseCase(StageRepository stageRepository, ApplicationRepository applicationRepository) {
        this.stageRepository = stageRepository;
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public StageResponse execute(CreateStageRequest request) {
        if (applicationRepository.findById(request.applicationId()).isEmpty()) {
            throw new DomainException("Candidatura não encontrada pelo id " + request.applicationId());
        }

        Stage stage = Stage.create(
                request.applicationId(),
                request.name(),
                request.orderIndex(),
                request.deadlineAt()
        );
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
