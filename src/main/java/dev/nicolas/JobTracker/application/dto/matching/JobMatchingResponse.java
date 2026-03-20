package dev.nicolas.JobTracker.application.dto.matching;

import java.util.List;

public record JobMatchingResponse(
        Integer score,
        Integer totalRequirements,
        Integer metRequirements,
        Integer unmetRequirements,
        Integer mustHaveUnmetRequirements,
        List<JobRequirementMatchResponse> requirements
) {
}
