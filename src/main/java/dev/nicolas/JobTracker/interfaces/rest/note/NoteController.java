package dev.nicolas.JobTracker.interfaces.rest.note;

import dev.nicolas.JobTracker.application.dto.note.CreateNoteRequest;
import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.application.dto.note.UpdateNoteRequest;
import dev.nicolas.JobTracker.application.usecases.note.create.CreateNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.delete.DeleteNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.get.GetNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.update.UpdateNoteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class NoteController {

    private final CreateNoteUseCase createNoteUseCase;
    private final GetNoteUseCase getNoteUseCase;
    private final DeleteNoteUseCase deleteNoteUseCase;
    private final UpdateNoteUseCase updateNoteUseCase;

    public NoteController(CreateNoteUseCase createNoteUseCase,
                          GetNoteUseCase getNoteUseCase,
                          DeleteNoteUseCase deleteNoteUseCase,
                          UpdateNoteUseCase updateNoteUseCase) {
        this.createNoteUseCase = createNoteUseCase;
        this.getNoteUseCase = getNoteUseCase;
        this.deleteNoteUseCase = deleteNoteUseCase;
        this.updateNoteUseCase = updateNoteUseCase;
    }

    @PostMapping("/notes")
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody CreateNoteRequest request) {
        NoteResponse response = createNoteUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/notes/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable UUID id) {
        return ResponseEntity.ok(getNoteUseCase.findById(id));
    }

    @GetMapping("/applications/{applicationId}/notes")
    public ResponseEntity<List<NoteResponse>> getNotesByApplicationId(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(getNoteUseCase.findByApplicationId(applicationId));
    }

    @PatchMapping("/notes/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateNoteRequest request) {
        return ResponseEntity.ok(updateNoteUseCase.execute(id, request));
    }

    @DeleteMapping("/notes/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable UUID id) {
        deleteNoteUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
