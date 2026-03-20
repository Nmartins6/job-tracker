package dev.nicolas.JobTracker.domain.note;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NoteRepository {

    Note save(Note note);

    Optional<Note> findById(UUID id);

    List<Note> findByApplicationId(UUID applicationId);
}
