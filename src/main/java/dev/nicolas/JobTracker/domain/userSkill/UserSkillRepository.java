package dev.nicolas.JobTracker.domain.userSkill;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserSkillRepository {

    UserSkill save(UserSkill userSkill);

    Optional<UserSkill> findById(UUID id);

    List<UserSkill> findByUserId(UUID userId);

    Optional<UserSkill> findByUserIdAndSkillId(UUID userId, UUID skillId);
}
