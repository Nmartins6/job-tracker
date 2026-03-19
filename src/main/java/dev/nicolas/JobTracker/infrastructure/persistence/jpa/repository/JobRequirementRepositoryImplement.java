package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirementRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobRequirementJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.JobRequirementMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JobRequirementRepositoryImplement implements JobRequirementRepository {

    private final JobRequirementJpaRepository jobRequirementJpaRepository;
    private final JobRequirementMapper mapper;

    public JobRequirementRepositoryImplement(JobRequirementJpaRepository jobRequirementJpaRepository,
                                             JobRequirementMapper mapper) {
        this.jobRequirementJpaRepository = jobRequirementJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public JobRequirement save(JobRequirement jobRequirement) {
        JobRequirementJpaEntity entity = mapper.toJpaEntity(jobRequirement);
        JobRequirementJpaEntity saved = jobRequirementJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<JobRequirement> findById(UUID id) {
        return jobRequirementJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<JobRequirement> findByJobId(UUID jobId) {
        return jobRequirementJpaRepository.findByJobId(jobId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<JobRequirement> findByJobIdAndSkillId(UUID jobId, UUID skillId) {
        return jobRequirementJpaRepository.findByJobIdAndSkillId(jobId, skillId).map(mapper::toDomain);
    }
}
