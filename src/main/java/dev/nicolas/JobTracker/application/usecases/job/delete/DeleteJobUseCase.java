package dev.nicolas.JobTracker.application.usecases.job.delete;

import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteJobUseCase {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRequirementRepository jobRequirementRepository;

    public DeleteJobUseCase(JobRepository jobRepository,
                            ApplicationRepository applicationRepository,
                            JobRequirementRepository jobRequirementRepository) {
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.jobRequirementRepository = jobRequirementRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (jobRepository.findById(id).isEmpty()) {
            throw new DomainException("Vaga não encontrada pelo id " + id);
        }

        if (applicationRepository.existsByJobId(id)) {
            throw new DomainException("Não é possível remover vaga com candidaturas vinculadas");
        }

        jobRequirementRepository.findByJobId(id)
                .forEach(requirement -> jobRequirementRepository.deleteById(requirement.getId()));

        jobRepository.deleteById(id);
    }
}
