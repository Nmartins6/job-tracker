package dev.nicolas.JobTracker.application.service.matching;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.dto.matching.JobRequirementMatchResponse;
import dev.nicolas.JobTracker.domain.jobRequirement.JobRequirement;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JobMatchingServiceTest {

    private final JobMatchingService jobMatchingService = new JobMatchingService();

    @Test
    void shouldReturnFullMatchWhenCandidateMeetsAllRequirements() {
        UUID javaSkillId = UUID.randomUUID();
        UUID springSkillId = UUID.randomUUID();

        List<UserSkill> userSkills = List.of(
                UserSkill.create(UUID.randomUUID(), javaSkillId, 4, 5),
                UserSkill.create(UUID.randomUUID(), springSkillId, 3, 4)
        );
        List<JobRequirement> jobRequirements = List.of(
                JobRequirement.create(UUID.randomUUID(), javaSkillId, true, 4, 3),
                JobRequirement.create(UUID.randomUUID(), springSkillId, false, 4, 2)
        );

        JobMatchingResponse response = jobMatchingService.compare(userSkills, jobRequirements);

        assertThat(response.score()).isEqualTo(100);
        assertThat(response.totalRequirements()).isEqualTo(2);
        assertThat(response.metRequirements()).isEqualTo(2);
        assertThat(response.unmetRequirements()).isEqualTo(0);
        assertThat(response.mustHaveUnmetRequirements()).isEqualTo(0);
        assertThat(response.requirements())
                .extracting(JobRequirementMatchResponse::matchPercentage)
                .containsExactly(100, 100);
    }

    @Test
    void shouldReturnPartialMatchAndIdentifyGaps() {
        UUID javaSkillId = UUID.randomUUID();
        UUID springSkillId = UUID.randomUUID();

        List<UserSkill> userSkills = List.of(
                UserSkill.create(UUID.randomUUID(), javaSkillId, 2, 2)
        );
        List<JobRequirement> jobRequirements = List.of(
                JobRequirement.create(UUID.randomUUID(), javaSkillId, true, 4, 3),
                JobRequirement.create(UUID.randomUUID(), springSkillId, false, 3, 2)
        );

        JobMatchingResponse response = jobMatchingService.compare(userSkills, jobRequirements);

        assertThat(response.score()).isEqualTo(30);
        assertThat(response.totalRequirements()).isEqualTo(2);
        assertThat(response.metRequirements()).isEqualTo(0);
        assertThat(response.unmetRequirements()).isEqualTo(2);
        assertThat(response.mustHaveUnmetRequirements()).isEqualTo(1);
        assertThat(response.requirements())
                .extracting(JobRequirementMatchResponse::gapLevel)
                .containsExactly(2, 3);
        assertThat(response.requirements())
                .extracting(JobRequirementMatchResponse::matchPercentage)
                .containsExactly(50, 0);
    }

    @Test
    void shouldReturnZeroScoreWhenThereAreNoRequirements() {
        JobMatchingResponse response = jobMatchingService.compare(List.of(), List.of());

        assertThat(response.score()).isZero();
        assertThat(response.totalRequirements()).isZero();
        assertThat(response.metRequirements()).isZero();
        assertThat(response.unmetRequirements()).isZero();
        assertThat(response.mustHaveUnmetRequirements()).isZero();
        assertThat(response.requirements()).isEmpty();
    }
}
