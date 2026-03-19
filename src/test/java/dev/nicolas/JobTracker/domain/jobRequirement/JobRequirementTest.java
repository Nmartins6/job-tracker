package dev.nicolas.JobTracker.domain.jobRequirement;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JobRequirementTest {

    @Test
    void shouldCreateJobRequirement() {
        UUID jobId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        JobRequirement jobRequirement = JobRequirement.create(jobId, skillId, true, 4, 3);

        assertThat(jobRequirement.getId()).isNotNull();
        assertThat(jobRequirement.getJobId()).isEqualTo(jobId);
        assertThat(jobRequirement.getSkillId()).isEqualTo(skillId);
        assertThat(jobRequirement.isMustHave()).isTrue();
        assertThat(jobRequirement.getDesiredLevel()).isEqualTo(4);
        assertThat(jobRequirement.getWeight()).isEqualTo(3);
    }

    @Test
    void shouldRejectJobRequirementWithoutJobId() {
        assertThatThrownBy(() -> JobRequirement.create(null, UUID.randomUUID(), true, 4, 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Vaga do requisito é obrigatória");
    }

    @Test
    void shouldRejectJobRequirementWithoutSkillId() {
        assertThatThrownBy(() -> JobRequirement.create(UUID.randomUUID(), null, true, 4, 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade do requisito é obrigatória");
    }

    @Test
    void shouldRejectJobRequirementWithInvalidDesiredLevel() {
        assertThatThrownBy(() -> JobRequirement.create(UUID.randomUUID(), UUID.randomUUID(), true, 6, 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nível desejado deve estar entre 1 e 5");
    }

    @Test
    void shouldRejectJobRequirementWithInvalidWeight() {
        assertThatThrownBy(() -> JobRequirement.create(UUID.randomUUID(), UUID.randomUUID(), true, 4, 0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Peso do requisito deve ser maior que zero");
    }
}
