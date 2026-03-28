package dev.nicolas.JobTracker.application.usecases.note.delete;

import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteNoteUseCase {

    private final NoteRepository noteRepository;

    public DeleteNoteUseCase(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (noteRepository.findById(id).isEmpty()) {
            throw new DomainException("Nota não encontrada pelo id " + id);
        }

        noteRepository.deleteById(id);
    }
}
