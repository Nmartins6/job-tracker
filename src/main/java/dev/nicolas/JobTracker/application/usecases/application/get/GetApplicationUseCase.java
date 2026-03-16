package dev.nicolas.JobTracker.application.usecases.application.get;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetApplicationUseCase {

    private final ApplicationRepository applicationRepository;

    public GetApplicationUseCase(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public ApplicationResponse findById(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new DomainException("Candidatura não encontrada pelo id " + id));

        return toResponse(application);
    }

    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ApplicationResponse toResponse(Application application) {
        return new ApplicationResponse(
                application.getId(),
                application.getUserId(),
                application.getJobId(),
                application.getStatus()
        );
    }
}
