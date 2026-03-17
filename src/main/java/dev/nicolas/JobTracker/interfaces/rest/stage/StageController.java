package dev.nicolas.JobTracker.interfaces.rest.stage;

import dev.nicolas.JobTracker.application.dto.stage.CreateStageRequest;
import dev.nicolas.JobTracker.application.dto.stage.StageResponse;
import dev.nicolas.JobTracker.application.usecases.stage.create.CreateStageUseCase;
import dev.nicolas.JobTracker.application.usecases.stage.get.GetStageUseCase;
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
public class StageController {

    private final CreateStageUseCase createStageUseCase;
    private final GetStageUseCase getStageUseCase;

    public StageController(CreateStageUseCase createStageUseCase, GetStageUseCase getStageUseCase) {
        this.createStageUseCase = createStageUseCase;
        this.getStageUseCase = getStageUseCase;
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
}
