package dev.nicolas.JobTracker.domain.application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {

    Application save(Application application);

    Optional<Application> findById(UUID id);

    List<Application> findAll();

    boolean existsByJobId(UUID jobId);

    void deleteById(UUID id);
}
