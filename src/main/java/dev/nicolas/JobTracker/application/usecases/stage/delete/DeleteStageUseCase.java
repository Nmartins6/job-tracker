package dev.nicolas.JobTracker.application.usecases.stage.delete;

import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteStageUseCase {

    private final StageRepository stageRepository;
    private final NoteRepository noteRepository;

    public DeleteStageUseCase(StageRepository stageRepository,
                              NoteRepository noteRepository) {
        this.stageRepository = stageRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (stageRepository.findById(id).isEmpty()) {
            throw new DomainException("Etapa não encontrada pelo id " + id);
        }

        if (noteRepository.existsByStageId(id)) {
            throw new DomainException("Etapa possui notas vinculadas e não pode ser removida");
        }

        stageRepository.deleteById(id);
    }
}
