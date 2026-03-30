package dev.nicolas.JobTracker.application.usecases.userSkill.delete;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.userSkill.UserSkill;
import dev.nicolas.JobTracker.domain.userSkill.UserSkillRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteUserSkillUseCaseTest {

    @Mock
    private UserSkillRepository userSkillRepository;

    @InjectMocks
    private DeleteUserSkillUseCase deleteUserSkillUseCase;

    @Test
    void shouldDeleteUserSkillWhenItExists() {
        UUID id = UUID.randomUUID();

        when(userSkillRepository.findById(id)).thenReturn(Optional.of(
                UserSkill.reconstitute(id, UUID.randomUUID(), UUID.randomUUID(), 2, 4)
        ));

        deleteUserSkillUseCase.execute(id);

        verify(userSkillRepository).deleteById(id);
    }

    @Test
    void shouldRejectDeleteWhenUserSkillDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(userSkillRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteUserSkillUseCase.execute(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Habilidade do usuário não encontrada pelo id " + id);

        verify(userSkillRepository, never()).deleteById(id);
    }
}
