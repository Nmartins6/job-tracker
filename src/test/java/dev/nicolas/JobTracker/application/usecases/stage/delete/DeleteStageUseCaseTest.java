package dev.nicolas.JobTracker.application.usecases.stage.delete;

import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
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
class DeleteStageUseCaseTest {

    @Mock
    private StageRepository stageRepository;

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private DeleteStageUseCase deleteStageUseCase;

    @Test
    void shouldDeleteStageWhenItExistsAndHasNoNotes() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.of(
                Stage.reconstitute(id, UUID.randomUUID(), "Screening", 1, null, null, null)
        ));
        when(noteRepository.existsByStageId(id)).thenReturn(false);

        deleteStageUseCase.execute(id);

        verify(stageRepository).deleteById(id);
    }

    @Test
    void shouldRejectDeleteWhenStageDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteStageUseCase.execute(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa não encontrada pelo id " + id);

        verify(noteRepository, never()).existsByStageId(id);
        verify(stageRepository, never()).deleteById(id);
    }

    @Test
    void shouldRejectDeleteWhenStageHasLinkedNotes() {
        UUID id = UUID.randomUUID();

        when(stageRepository.findById(id)).thenReturn(Optional.of(
                Stage.reconstitute(id, UUID.randomUUID(), "Entrevista", 2, null, null, null)
        ));
        when(noteRepository.existsByStageId(id)).thenReturn(true);

        assertThatThrownBy(() -> deleteStageUseCase.execute(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Etapa possui notas vinculadas e não pode ser removida");

        verify(stageRepository, never()).deleteById(id);
    }
}
