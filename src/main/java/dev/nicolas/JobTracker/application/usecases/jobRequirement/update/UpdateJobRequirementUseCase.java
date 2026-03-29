package dev.nicolas.JobTracker.application.usecases.jobRequirement.update;

import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.application.dto.jobRequirement.UpdateJobRequirementRequest;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.skill.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateJobRequirementUseCase {

    private final JobRequirementRepository jobRequirementRepository;
    private final SkillRepository skillRepository;

    public UpdateJobRequirementUseCase(JobRequirementRepository jobRequirementRepository,
                                       SkillRepository skillRepository) {
        this.jobRequirementRepository = jobRequirementRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public JobRequirementResponse execute(UUID id, UpdateJobRequirementRequest request) {
        JobRequirement existing = jobRequirementRepository.findById(id)
                .orElseThrow(() -> new DomainException("Requisito da vaga não encontrado pelo id " + id));

        if (skillRepository.findById(request.skillId()).isEmpty()) {
            throw new DomainException("Habilidade não encontrada pelo id " + request.skillId());
        }

        jobRequirementRepository.findByJobIdAndSkillId(existing.getJobId(), request.skillId())
                .filter(jobRequirement -> !jobRequirement.getId().equals(id))
                .ifPresent(jobRequirement -> {
                    throw new DomainException("Requisito já cadastrado para a vaga");
                });

        JobRequirement validated = JobRequirement.create(
                existing.getJobId(),
                request.skillId(),
                request.mustHave(),
                request.desiredLevel(),
                request.weight()
        );

        JobRequirement saved = jobRequirementRepository.save(JobRequirement.reconstitute(
                existing.getId(),
                existing.getJobId(),
                validated.getSkillId(),
                validated.isMustHave(),
                validated.getDesiredLevel(),
                validated.getWeight()
        ));

        return new JobRequirementResponse(
                saved.getId(),
                saved.getJobId(),
                saved.getSkillId(),
                saved.isMustHave(),
                saved.getDesiredLevel(),
                saved.getWeight()
        );
    }
}
