package dev.nicolas.JobTracker.application.usecases.matching.get;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.service.matching.JobMatchingService;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetJobMatchingUseCase {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final UserSkillRepository userSkillRepository;
    private final JobRequirementRepository jobRequirementRepository;
    private final JobMatchingService jobMatchingService;

    public GetJobMatchingUseCase(UserRepository userRepository,
                                 JobRepository jobRepository,
                                 UserSkillRepository userSkillRepository,
                                 JobRequirementRepository jobRequirementRepository,
                                 JobMatchingService jobMatchingService) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.userSkillRepository = userSkillRepository;
        this.jobRequirementRepository = jobRequirementRepository;
        this.jobMatchingService = jobMatchingService;
    }

    public JobMatchingResponse execute(UUID userId, UUID jobId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new DomainException("Usuário não encontrado pelo id " + userId);
        }

        if (jobRepository.findById(jobId).isEmpty()) {
            throw new DomainException("Vaga não encontrada pelo id " + jobId);
        }

        return jobMatchingService.compare(
                userSkillRepository.findByUserId(userId),
                jobRequirementRepository.findByJobId(jobId)
        );
    }
}
