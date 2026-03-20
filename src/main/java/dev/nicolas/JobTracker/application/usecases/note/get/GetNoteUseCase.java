package dev.nicolas.JobTracker.application.usecases.note.get;

import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetNoteUseCase {

    private final NoteRepository noteRepository;

    public GetNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public NoteResponse findById(UUID id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new DomainException("Nota não encontrada pelo id " + id));

        return toResponse(note);
    }

    public List<NoteResponse> findByApplicationId(UUID applicationId) {
        return noteRepository.findByApplicationId(applicationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private NoteResponse toResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getApplicationId(),
                note.getStageId(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}
