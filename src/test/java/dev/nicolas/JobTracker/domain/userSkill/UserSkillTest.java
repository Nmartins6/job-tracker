package dev.nicolas.JobTracker.domain.userSkill;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSkillTest {

    @Test
    void shouldCreateUserSkillWithValidData() {
        UUID userId = UUID.randomUUID();
        UUID skillId = UUID.randomUUID();

        UserSkill userSkill = UserSkill.create(userId, skillId, 3, 4);

        assertThat(userSkill.getId()).isNotNull();
        assertThat(userSkill.getUserId()).isEqualTo(userId);
        assertThat(userSkill.getSkillId()).isEqualTo(skillId);
        assertThat(userSkill.getYearsExperience()).isEqualTo(3);
        assertThat(userSkill.getLevel()).isEqualTo(4);
    }

    @Test
    void shouldRejectNegativeYearsExperience() {
        assertThatThrownBy(() -> UserSkill.create(UUID.randomUUID(), UUID.randomUUID(), -1, 3))
                .isInstanceOf(DomainException.class)
                .hasMessage("Anos de experiência não pode ser negativo");
    }

    @Test
    void shouldRejectLevelBelowAllowedRange() {
        assertThatThrownBy(() -> UserSkill.create(UUID.randomUUID(), UUID.randomUUID(), 2, 0))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nível da habilidade deve estar entre 1 e 5");
    }

    @Test
    void shouldRejectLevelAboveAllowedRange() {
        assertThatThrownBy(() -> UserSkill.create(UUID.randomUUID(), UUID.randomUUID(), 2, 6))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nível da habilidade deve estar entre 1 e 5");
    }
}
