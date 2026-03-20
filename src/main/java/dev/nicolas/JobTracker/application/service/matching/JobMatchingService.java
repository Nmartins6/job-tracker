package dev.nicolas.JobTracker.application.service.matching;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.dto.matching.JobRequirementMatchResponse;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    public JobMatchingResponse compare(List<UserSkill> userSkills, List<JobRequirement> jobRequirements) {
        List<UserSkill> safeUserSkills = userSkills == null ? List.of() : userSkills;
        List<JobRequirement> safeJobRequirements = jobRequirements == null ? List.of() : jobRequirements;

        Map<UUID, UserSkill> userSkillsBySkillId = safeUserSkills.stream()
                .collect(Collectors.toMap(UserSkill::getSkillId, Function.identity(), (first, second) -> first));

        List<JobRequirementMatchResponse> requirementMatches = safeJobRequirements.stream()
                .map(requirement -> toRequirementMatch(requirement, userSkillsBySkillId.get(requirement.getSkillId())))
                .toList();

        int totalRequirements = requirementMatches.size();
        int metRequirements = (int) requirementMatches.stream()
                .filter(JobRequirementMatchResponse::met)
                .count();
        int unmetRequirements = totalRequirements - metRequirements;
        int mustHaveUnmetRequirements = (int) requirementMatches.stream()
                .filter(match -> match.mustHave() && !match.met())
                .count();

        double totalWeight = safeJobRequirements.stream()
                .mapToInt(JobRequirement::getWeight)
                .sum();
        double matchedWeight = safeJobRequirements.stream()
                .mapToDouble(requirement -> requirement.getWeight()
                        * calculateCoverageRatio(requirement, userSkillsBySkillId.get(requirement.getSkillId())))
                .sum();

        int score = totalWeight == 0
                ? 0
                : (int) Math.round((matchedWeight / totalWeight) * 100);

        return new JobMatchingResponse(
                score,
                totalRequirements,
                metRequirements,
                unmetRequirements,
                mustHaveUnmetRequirements,
                requirementMatches
        );
    }

    private JobRequirementMatchResponse toRequirementMatch(JobRequirement requirement, UserSkill userSkill) {
        Integer candidateLevel = userSkill == null ? null : userSkill.getLevel();
        boolean met = candidateLevel != null && candidateLevel >= requirement.getDesiredLevel();
        int gapLevel = candidateLevel == null
                ? requirement.getDesiredLevel()
                : Math.max(0, requirement.getDesiredLevel() - candidateLevel);
        int matchPercentage = (int) Math.round(calculateCoverageRatio(requirement, userSkill) * 100);

        return new JobRequirementMatchResponse(
                requirement.getSkillId(),
                requirement.isMustHave(),
                requirement.getDesiredLevel(),
                candidateLevel,
                requirement.getWeight(),
                met,
                gapLevel,
                matchPercentage
        );
    }

    private double calculateCoverageRatio(JobRequirement requirement, UserSkill userSkill) {
        if (userSkill == null) {
            return 0;
        }

        return Math.min(userSkill.getLevel(), requirement.getDesiredLevel()) / (double) requirement.getDesiredLevel();
    }
}
