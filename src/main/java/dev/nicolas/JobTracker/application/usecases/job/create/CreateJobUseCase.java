package dev.nicolas.JobTracker.application.usecases.job.create;

import dev.nicolas.JobTracker.application.dto.job.CreateJobRequest;
import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateJobUseCase {

    private final JobRepository jobRepository;

    public CreateJobUseCase(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Transactional
    public JobResponse execute(CreateJobRequest request) {
        Job job = Job.create(
                request.company(),
                request.title(),
                request.sourceUrl(),
                request.seniority(),
                request.location(),
                request.description()
        );

        Job saved = jobRepository.save(job);

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
