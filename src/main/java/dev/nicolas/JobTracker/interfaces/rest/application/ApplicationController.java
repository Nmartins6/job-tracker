package dev.nicolas.JobTracker.interfaces.rest.application;

import dev.nicolas.JobTracker.application.dto.application.ApplicationResponse;
import dev.nicolas.JobTracker.application.dto.application.CreateApplicationRequest;
import dev.nicolas.JobTracker.application.usecases.application.create.CreateApplicationUseCase;
import dev.nicolas.JobTracker.application.usecases.application.get.GetApplicationUseCase;
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
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final CreateApplicationUseCase createApplicationUseCase;
    private final GetApplicationUseCase getApplicationUseCase;

    public ApplicationController(CreateApplicationUseCase createApplicationUseCase,
                                 GetApplicationUseCase getApplicationUseCase) {
        this.createApplicationUseCase = createApplicationUseCase;
        this.getApplicationUseCase = getApplicationUseCase;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody CreateApplicationRequest request) {
        ApplicationResponse response = createApplicationUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(getApplicationUseCase.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable UUID id) {
        return ResponseEntity.ok(getApplicationUseCase.findById(id));
    }
}
