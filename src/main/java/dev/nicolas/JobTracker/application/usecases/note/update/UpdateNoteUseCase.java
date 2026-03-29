package dev.nicolas.JobTracker.application.usecases.note.update;

import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.application.dto.note.UpdateNoteRequest;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateNoteUseCase {

    private final NoteRepository noteRepository;
    private final StageRepository stageRepository;

    public UpdateNoteUseCase(NoteRepository noteRepository,
                             StageRepository stageRepository) {
        this.noteRepository = noteRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public NoteResponse execute(UUID id, UpdateNoteRequest request) {
        Note existing = noteRepository.findById(id)
                .orElseThrow(() -> new DomainException("Nota não encontrada pelo id " + id));

        if (request.stageId() != null) {
            Stage stage = stageRepository.findById(request.stageId())
                    .orElseThrow(() -> new DomainException("Etapa não encontrada pelo id " + request.stageId()));

            if (!stage.getApplicationId().equals(existing.getApplicationId())) {
                throw new DomainException("Etapa não pertence à candidatura informada");
            }
        }

        Note validated = Note.create(
                existing.getApplicationId(),
                request.stageId(),
                request.content(),
                existing.getCreatedAt()
        );

        Note saved = noteRepository.save(Note.reconstitute(
                existing.getId(),
                existing.getApplicationId(),
                validated.getStageId(),
                validated.getContent(),
                existing.getCreatedAt()
        ));

        return new NoteResponse(
                saved.getId(),
                saved.getApplicationId(),
                saved.getStageId(),
                saved.getContent(),
                saved.getCreatedAt()
        );
    }
}
