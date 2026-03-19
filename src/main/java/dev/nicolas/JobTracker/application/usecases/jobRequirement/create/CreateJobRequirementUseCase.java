package dev.nicolas.JobTracker.application.usecases.jobRequirement.create;

import dev.nicolas.JobTracker.application.dto.jobRequirement.CreateJobRequirementRequest;
import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateJobRequirementUseCase {

    private final JobRequirementRepository jobRequirementRepository;
    private final JobRepository jobRepository;
    private final SkillRepository skillRepository;

    public CreateJobRequirementUseCase(JobRequirementRepository jobRequirementRepository,
                                       JobRepository jobRepository,
                                       SkillRepository skillRepository) {
        this.jobRequirementRepository = jobRequirementRepository;
        this.jobRepository = jobRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public JobRequirementResponse execute(CreateJobRequirementRequest request) {
        if (jobRepository.findById(request.jobId()).isEmpty()) {
            throw new DomainException("Vaga não encontrada pelo id " + request.jobId());
        }

        if (skillRepository.findById(request.skillId()).isEmpty()) {
            throw new DomainException("Habilidade não encontrada pelo id " + request.skillId());
        }

        if (jobRequirementRepository.findByJobIdAndSkillId(request.jobId(), request.skillId()).isPresent()) {
            throw new DomainException("Requisito já cadastrado para a vaga");
        }

        JobRequirement jobRequirement = JobRequirement.create(
                request.jobId(),
                request.skillId(),
                request.mustHave(),
                request.desiredLevel(),
                request.weight()
        );
        JobRequirement saved = jobRequirementRepository.save(jobRequirement);

        return toResponse(saved);
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
