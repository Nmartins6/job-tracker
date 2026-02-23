package dev.nicolas.JobTracker.domain.job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository {

    Job save(Job job);

    Optional<Job> findById(UUID id);

    List<Job> findAll();

    void deleteById(UUID id);
}
