package dev.nicolas.JobTracker.domain.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);

    Optional<User> findById(UUID id);

    List<User> findAll();

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
