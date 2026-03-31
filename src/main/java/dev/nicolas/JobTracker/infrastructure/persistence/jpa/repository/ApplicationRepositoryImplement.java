package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.application.Application;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.ApplicationJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.ApplicationMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApplicationRepositoryImplement implements ApplicationRepository {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final ApplicationMapper mapper;

    public ApplicationRepositoryImplement(ApplicationJpaRepository applicationJpaRepository, ApplicationMapper mapper) {
        this.applicationJpaRepository = applicationJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Application save(Application application) {
        ApplicationJpaEntity entity = mapper.toJpaEntity(application);
        ApplicationJpaEntity saved = applicationJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Application> findById(UUID id) {
        return applicationJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Application> findAll() {
        return applicationJpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsByJobId(UUID jobId) {
        return applicationJpaRepository.existsByJobId(jobId);
    }

    @Override
    public void deleteById(UUID id) {
        applicationJpaRepository.deleteById(id);
    }
}
