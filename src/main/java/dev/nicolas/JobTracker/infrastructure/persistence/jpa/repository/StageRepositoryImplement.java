package dev.nicolas.JobTracker.infrastructure.persistence.jpa.repository;

import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.entity.StageJpaEntity;
import dev.nicolas.JobTracker.infrastructure.persistence.jpa.mapper.StageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StageRepositoryImplement implements StageRepository {

    private final StageJpaRepository stageJpaRepository;
    private final StageMapper mapper;

    public StageRepositoryImplement(StageJpaRepository stageJpaRepository, StageMapper mapper) {
        this.stageJpaRepository = stageJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Stage save(Stage stage) {
        StageJpaEntity entity = mapper.toJpaEntity(stage);
        StageJpaEntity saved = stageJpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Stage> findById(UUID id) {
        return stageJpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Stage> findByApplicationId(UUID applicationId) {
        return stageJpaRepository.findByApplicationIdOrderByOrderIndexAsc(applicationId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        stageJpaRepository.deleteById(id);
    }
}
