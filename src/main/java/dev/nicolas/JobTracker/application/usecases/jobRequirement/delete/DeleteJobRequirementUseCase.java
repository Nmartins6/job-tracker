package dev.nicolas.JobTracker.application.usecases.jobRequirement.delete;

import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteJobRequirementUseCase {

    private final JobRequirementRepository jobRequirementRepository;

    public DeleteJobRequirementUseCase(JobRequirementRepository jobRequirementRepository) {
        this.jobRequirementRepository = jobRequirementRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (jobRequirementRepository.findById(id).isEmpty()) {
            throw new DomainException("Requisito da vaga não encontrado pelo id " + id);
        }

        jobRequirementRepository.deleteById(id);
    }
}
