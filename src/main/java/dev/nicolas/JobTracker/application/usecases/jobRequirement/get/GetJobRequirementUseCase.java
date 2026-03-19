package dev.nicolas.JobTracker.application.usecases.jobRequirement.get;

import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetJobRequirementUseCase {

    private final JobRequirementRepository jobRequirementRepository;

    public GetJobRequirementUseCase(JobRequirementRepository jobRequirementRepository) {
        this.jobRequirementRepository = jobRequirementRepository;
    }

    public JobRequirementResponse findById(UUID id) {
        JobRequirement jobRequirement = jobRequirementRepository.findById(id)
                .orElseThrow(() -> new DomainException("Requisito da vaga não encontrado pelo id " + id));

        return toResponse(jobRequirement);
    }

    public List<JobRequirementResponse> findByJobId(UUID jobId) {
        return jobRequirementRepository.findByJobId(jobId).stream()
                .map(this::toResponse)
                .toList();
    }

    private JobRequirementResponse toResponse(JobRequirement jobRequirement) {
        return new JobRequirementResponse(
                jobRequirement.getId(),
                jobRequirement.getJobId(),
                jobRequirement.getSkillId(),
                jobRequirement.isMustHave(),
                jobRequirement.getDesiredLevel(),
                jobRequirement.getWeight()
        );
    }
}
