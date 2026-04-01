package dev.nicolas.JobTracker.application.usecases.application.create;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.CreateApplicationRequest;
import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateApplicationUseCase {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public CreateApplicationUseCase(ApplicationRepository applicationRepository,
                                    UserRepository userRepository,
                                    JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }

    @Transactional
    public ApplicationResponse execute(CreateApplicationRequest request) {
        if (userRepository.findById(request.userId()).isEmpty()) {
            throw new DomainException("Usuário não encontrado pelo id " + request.userId());
        }

        if (jobRepository.findById(request.jobId()).isEmpty()) {
            throw new DomainException("Vaga não encontrada pelo id " + request.jobId());
        }

        Application application = Application.create(
                request.userId(),
                request.jobId(),
                request.nextAction(),
                request.nextActionDueAt()
        );
        Application saved = applicationRepository.save(application);

        return new ApplicationResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getJobId(),
                saved.getStatus(),
                saved.getNextAction(),
                saved.getNextActionDueAt()
        );
    }
}
