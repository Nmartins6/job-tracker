package dev.nicolas.JobTracker.application.usecases.application.delete;

import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DeleteApplicationUseCase {

    private final ApplicationRepository applicationRepository;
    private final NoteRepository noteRepository;
    private final StageRepository stageRepository;

    public DeleteApplicationUseCase(ApplicationRepository applicationRepository,
                                    NoteRepository noteRepository,
                                    StageRepository stageRepository) {
        this.applicationRepository = applicationRepository;
        this.noteRepository = noteRepository;
        this.stageRepository = stageRepository;
    }

    @Transactional
    public void execute(UUID id) {
        if (applicationRepository.findById(id).isEmpty()) {
            throw new DomainException("Candidatura não encontrada pelo id " + id);
        }

        noteRepository.findByApplicationId(id)
                .forEach(note -> noteRepository.deleteById(note.getId()));

        stageRepository.findByApplicationId(id)
                .forEach(stage -> stageRepository.deleteById(stage.getId()));

        applicationRepository.deleteById(id);
    }
}
