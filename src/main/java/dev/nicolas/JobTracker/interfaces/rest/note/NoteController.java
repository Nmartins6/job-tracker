package dev.nicolas.JobTracker.interfaces.rest.note;

import dev.nicolas.JobTracker.application.dto.note.CreateNoteRequest;
import dev.nicolas.JobTracker.application.dto.note.NoteResponse;
import dev.nicolas.JobTracker.application.usecases.note.create.CreateNoteUseCase;
import dev.nicolas.JobTracker.application.usecases.note.get.GetNoteUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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

    public NoteController(CreateNoteUseCase createNoteUseCase,
                          GetNoteUseCase getNoteUseCase) {
        this.createNoteUseCase = createNoteUseCase;
        this.getNoteUseCase = getNoteUseCase;
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
}
