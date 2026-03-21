package dev.nicolas.JobTracker.application.usecases.application.history;

import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventResponse;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryEventType;
import dev.nicolas.JobTracker.application.dto.history.ApplicationHistoryResponse;
import dev.nicolas.JobTracker.domain.application.ApplicationRepository;
import dev.nicolas.JobTracker.domain.note.Note;
import dev.nicolas.JobTracker.domain.note.NoteRepository;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.stage.Stage;
import dev.nicolas.JobTracker.domain.stage.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GetApplicationHistoryUseCase {

    private final ApplicationRepository applicationRepository;
    private final StageRepository stageRepository;
    private final NoteRepository noteRepository;

    public GetApplicationHistoryUseCase(ApplicationRepository applicationRepository,
                                        StageRepository stageRepository,
                                        NoteRepository noteRepository) {
        this.applicationRepository = applicationRepository;
        this.stageRepository = stageRepository;
        this.noteRepository = noteRepository;
    }

    public ApplicationHistoryResponse execute(UUID applicationId) {
        if (applicationRepository.findById(applicationId).isEmpty()) {
            throw new DomainException("Candidatura não encontrada pelo id " + applicationId);
        }

        List<Stage> stages = stageRepository.findByApplicationId(applicationId);
        List<Note> notes = noteRepository.findByApplicationId(applicationId);

        Map<UUID, Stage> stagesById = stages.stream()
                .collect(Collectors.toMap(Stage::getId, Function.identity()));

        List<ApplicationHistoryEventResponse> events = new ArrayList<>();
        events.addAll(toStageEvents(stages));
        events.addAll(toNoteEvents(notes, stagesById));

        List<ApplicationHistoryEventResponse> orderedEvents = events.stream()
                .sorted(Comparator
                        .comparing(ApplicationHistoryEventResponse::occurredAt)
                        .thenComparing(ApplicationHistoryEventResponse::type)
                        .thenComparing(ApplicationHistoryEventResponse::referenceId))
                .toList();

        return new ApplicationHistoryResponse(applicationId, orderedEvents);
    }

    private List<ApplicationHistoryEventResponse> toStageEvents(List<Stage> stages) {
        List<ApplicationHistoryEventResponse> events = new ArrayList<>();

        for (Stage stage : stages) {
            if (stage.getStartedAt() != null) {
                events.add(new ApplicationHistoryEventResponse(
                        ApplicationHistoryEventType.STAGE_STARTED,
                        stage.getId(),
                        stage.getId(),
                        stage.getName(),
                        "Etapa iniciada",
                        stage.getStartedAt()
                ));
            }

            if (stage.getCompletedAt() != null) {
                events.add(new ApplicationHistoryEventResponse(
                        ApplicationHistoryEventType.STAGE_COMPLETED,
                        stage.getId(),
                        stage.getId(),
                        stage.getName(),
                        "Etapa concluída",
                        stage.getCompletedAt()
                ));
            }

            if (stage.getDeadlineAt() != null) {
                events.add(new ApplicationHistoryEventResponse(
                        ApplicationHistoryEventType.STAGE_DEADLINE,
                        stage.getId(),
                        stage.getId(),
                        stage.getName(),
                        "Prazo da etapa",
                        stage.getDeadlineAt()
                ));
            }
        }

        return events;
    }

    private List<ApplicationHistoryEventResponse> toNoteEvents(List<Note> notes, Map<UUID, Stage> stagesById) {
        return notes.stream()
                .map(note -> {
                    Stage stage = note.getStageId() == null ? null : stagesById.get(note.getStageId());
                    String title = stage == null
                            ? "Nota da candidatura"
                            : "Nota da etapa " + stage.getName();

                    return new ApplicationHistoryEventResponse(
                            ApplicationHistoryEventType.NOTE,
                            note.getId(),
                            note.getStageId(),
                            title,
                            note.getContent(),
                            note.getCreatedAt()
                    );
                })
                .toList();
    }
}
