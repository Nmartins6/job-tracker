package dev.nicolas.JobTracker.domain.stage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StageRepository {

    Stage save(Stage stage);

    Optional<Stage> findById(UUID id);

    List<Stage> findByApplicationId(UUID applicationId);
}
