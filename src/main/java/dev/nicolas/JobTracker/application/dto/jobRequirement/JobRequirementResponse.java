package dev.nicolas.JobTracker.application.dto.jobRequirement;

import java.util.UUID;

public record JobRequirementResponse(
        UUID id,
        UUID jobId,
        UUID skillId,
        Boolean mustHave,
        Integer desiredLevel,
        Integer weight
) {
}
