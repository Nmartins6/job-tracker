package dev.nicolas.JobTracker.application.usecases.job.update;

import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.application.dto.job.UpdateJobRequest;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateJobUseCase {

    private final JobRepository jobRepository;

    public UpdateJobUseCase(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse execute(UUID id, UpdateJobRequest request) {
        if (jobRepository.findById(id).isEmpty()) {
            throw new DomainException("Vaga não encontrada pelo id " + id);
        }

        Job normalized = Job.create(
                request.company(),
                request.title(),
                request.sourceUrl(),
                request.seniority(),
                request.location(),
                request.description()
        );

        Job updated = Job.reconstitute(
                id,
                normalized.getCompany(),
                normalized.getTitle(),
                normalized.getSourceUrl(),
                normalized.getSeniority(),
                normalized.getLocation(),
                normalized.getDescription()
        );

        Job saved = jobRepository.save(updated);

        return new JobResponse(
                saved.getId(),
                saved.getCompany(),
                saved.getTitle(),
                saved.getSourceUrl(),
                saved.getSeniority(),
                saved.getLocation(),
                saved.getDescription()
        );
    }
}
