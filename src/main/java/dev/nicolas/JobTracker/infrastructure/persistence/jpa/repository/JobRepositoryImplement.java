package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.job.Job;
import dev.nicolas.JobTracker.domain.job.JobRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.JobJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.JobMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class JobRepositoryImplement implements JobRepository {

    private final JobJpaRepository jobJpaRepository;
    private final JobMapper mapper;

    public JobRepositoryImplement(JobJpaRepository jobJpaRepository, JobMapper mapper) {
        this.jobJpaRepository = jobJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Job save(Job job) {
        JobJpaEntity entity = mapper.toJpaEntity(job);
        JobJpaEntity saved = jobJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Job> findById(UUID id) {
        return jobJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Job> findAll() {
        return jobJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        jobJpaRepository.deleteById(id);
    }

}
