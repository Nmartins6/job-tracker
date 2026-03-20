package dev.nicolas.JobTracker.application.usecases.note.get;

import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetNoteUseCaseTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private GetNoteUseCase getNoteUseCase;

    @Test
    void shouldReturnNoteById() {
        UUID id = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID stageId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();
        Note note = Note.reconstitute(id, applicationId, stageId, "Feedback", createdAt);

        when(noteRepository.findById(id)).thenReturn(Optional.of(note));

        NoteResponse response = getNoteUseCase.findById(id);

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.applicationId()).isEqualTo(applicationId);
        assertThat(response.stageId()).isEqualTo(stageId);
        assertThat(response.content()).isEqualTo("Feedback");
        assertThat(response.createdAt()).isEqualTo(createdAt);
    }

    @Test
    void shouldThrowWhenNoteIsNotFoundById() {
        UUID id = UUID.randomUUID();

        when(noteRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getNoteUseCase.findById(id))
                .isInstanceOf(DomainException.class)
                .hasMessage("Nota não encontrada pelo id " + id);
    }

    @Test
    void shouldReturnNotesByApplicationId() {
        UUID applicationId = UUID.randomUUID();
        Note first = Note.reconstitute(UUID.randomUUID(), applicationId, null, "Primeira nota", LocalDateTime.now());
        Note second = Note.reconstitute(UUID.randomUUID(), applicationId, UUID.randomUUID(), "Segunda nota", LocalDateTime.now().plusMinutes(10));

        when(noteRepository.findByApplicationId(applicationId)).thenReturn(List.of(first, second));

        List<NoteResponse> responses = getNoteUseCase.findByApplicationId(applicationId);

        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(NoteResponse::content)
                .containsExactly("Primeira nota", "Segunda nota");
    }
}
