package dev.nicolas.JobTracker.domain.jobRequirement;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRequirementRepository {

    JobRequirement save(JobRequirement jobRequirement);

    Optional<JobRequirement> findById(UUID id);

    List<JobRequirement> findByJobId(UUID jobId);

    Optional<JobRequirement> findByJobIdAndSkillId(UUID jobId, UUID skillId);
}
