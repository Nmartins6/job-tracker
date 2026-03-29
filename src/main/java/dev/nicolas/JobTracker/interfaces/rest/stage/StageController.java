package dev.nicolas.JobTracker.interfaces.rest.stage;

import dev.nicolas.JobTracker.application.dto.stage.CompleteStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.CreateStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StartStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.dto.stage.UpdateStageRequest;
import dev.nicolas.JobTracker.application.usecases.stage.complete.CompleteStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.create.CreateStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.delete.DeleteStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.get.GetStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.start.StartStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.update.UpdateStageUseCase;
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
public class StageController {

    private final CreateStageUseCase createStageUseCase;
    private final GetStageUseCase getStageUseCase;
    private final StartStageUseCase startStageUseCase;
    private final CompleteStageUseCase completeStageUseCase;
    private final DeleteStageUseCase deleteStageUseCase;
    private final UpdateStageUseCase updateStageUseCase;

    public StageController(CreateStageUseCase createStageUseCase,
                           GetStageUseCase getStageUseCase,
                           StartStageUseCase startStageUseCase,
                           CompleteStageUseCase completeStageUseCase,
                           DeleteStageUseCase deleteStageUseCase,
                           UpdateStageUseCase updateStageUseCase) {
        this.createStageUseCase = createStageUseCase;
        this.getStageUseCase = getStageUseCase;
        this.startStageUseCase = startStageUseCase;
        this.completeStageUseCase = completeStageUseCase;
        this.deleteStageUseCase = deleteStageUseCase;
        this.updateStageUseCase = updateStageUseCase;
    }

    @PostMapping("/stages")
    public ResponseEntity<StageResponse> createStage(@Valid @RequestBody CreateStageRequest request) {
        StageResponse response = createStageUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/stages/{id}")
    public ResponseEntity<StageResponse> getStageById(@PathVariable UUID id) {
        return ResponseEntity.ok(getStageUseCase.findById(id));
    }

    @GetMapping("/applications/{applicationId}/stages")
    public ResponseEntity<List<StageResponse>> getStagesByApplicationId(@PathVariable UUID applicationId) {
        return ResponseEntity.ok(getStageUseCase.findByApplicationId(applicationId));
    }

    @PatchMapping("/stages/{id}/start")
    public ResponseEntity<StageResponse> startStage(@PathVariable UUID id,
                                                    @Valid @RequestBody StartStageRequest request) {
        return ResponseEntity.ok(startStageUseCase.execute(id, request));
    }

    @PatchMapping("/stages/{id}/complete")
    public ResponseEntity<StageResponse> completeStage(@PathVariable UUID id,
                                                       @Valid @RequestBody CompleteStageRequest request) {
        return ResponseEntity.ok(completeStageUseCase.execute(id, request));
    }

    @PatchMapping("/stages/{id}")
    public ResponseEntity<StageResponse> updateStage(@PathVariable UUID id,
                                                     @Valid @RequestBody UpdateStageRequest request) {
        return ResponseEntity.ok(updateStageUseCase.execute(id, request));
    }

    @DeleteMapping("/stages/{id}")
    public ResponseEntity<Void> deleteStage(@PathVariable UUID id) {
        deleteStageUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
