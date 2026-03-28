package dev.nicolas.JobTracker.application.usecases.note.delete;

import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteNoteUseCaseTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private DeleteNoteUseCase deleteNoteUseCase;

    @Test
    void shouldDeleteNoteWhenItExists() {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.of(
                Note.reconstitute(
                        id,
                        UUID.randomUUID(),
                        null,
                        "Observação",
                        LocalDateTime.now()
                )
        ));

        deleteNoteUseCase.execute(id);

        verify(noteRepository).deleteById(id);
    }

    @Test
    void shouldRejectDeleteWhenNoteDoesNotExist() {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deleteNoteUseCase.execute(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nota não encontrada pelo id " + id);

        verify(noteRepository, never()).deleteById(id);
    }
}
