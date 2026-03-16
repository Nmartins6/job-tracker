package dev.nicolas.JobTracker.application.usecases.application.update;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.UpdateApplicationStatusRequest;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateApplicationStatusUseCase {

    private final ApplicationRepository applicationRepository;

    public UpdateApplicationStatusUseCase(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    @Transactional
    public ApplicationResponse execute(UUID id, UpdateApplicationStatusRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new DomainException("Candidatura não encontrada pelo id " + id));

        application.updateStatus(request.status());
        Application saved = applicationRepository.save(application);

        return new ApplicationResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getJobId(),
                saved.getStatus()
        );
    }
}
