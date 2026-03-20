package dev.nicolas.JobTracker.application.usecases.note.create;

import dev.nicolas.JobTracker.application.dto.note.CreateNoteRequest;
import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateNoteUseCase {

    private final NoteRepository noteRepository;
    private final ApplicationRepository applicationRepository;
    private final StageRepository stageRepository;

    public CreateNoteUseCase(NoteRepository noteRepository,
                             ApplicationRepository applicationRepository,
                             StageRepository stageRepository) {
        this.noteRepository = noteRepository;
        this.applicationRepository = applicationRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public NoteResponse execute(CreateNoteRequest request) {
        if (applicationRepository.findById(request.applicationId()).isEmpty()) {
            throw new DomainException("Candidatura não encontrada pelo id " + request.applicationId());
        }

        if (request.stageId() != null) {
            Stage stage = stageRepository.findById(request.stageId())
                    .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + request.stageId()));

            if (!stage.getApplicationId().equals(request.applicationId())) {
                throw new DomainException("Etapa não pertence à candidatura informada");
            }
        }

        Note note = Note.create(
                request.applicationId(),
                request.stageId(),
                request.content(),
                LocalDateTime.now()
        );
        Note saved = noteRepository.save(note);

        return toResponse(saved);
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
