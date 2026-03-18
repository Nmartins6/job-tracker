package dev.nicolas.JobTracker.application.dto.userSkill;

import java.util.UUID;

public record UserSkillResponse(
        UUID id,
        UUID userId,
        UUID skillId,
        Integer yearsExperience,
        Integer level
) {
}
