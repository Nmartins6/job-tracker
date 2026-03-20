package dev.nicolas.JobTracker.application.dto.matching;

import java.util.UUID;

public record JobRequirementMatchResponse(
        UUID skillId,
        Boolean mustHave,
        Integer desiredLevel,
        Integer candidateLevel,
        Integer weight,
        Boolean met,
        Integer gapLevel,
        Integer matchPercentage
) {
}
