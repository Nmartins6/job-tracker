package dev.nicolas.JobTracker.interfaces.rest.jobRequirement;

import dev.nicolas.JobTracker.application.dto.jobRequirement.CreateJobRequirementRequest;
import dev.nicolas.JobTracker.application.dto.jobRequirement.JobRequirementResponse;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.create.CreateJobRequirementUseCase;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.delete.DeleteJobRequirementUseCase;
import dev.nicolas.JobTracker.application.usecases.jobRequirement.get.GetJobRequirementUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class JobRequirementController {

    private final CreateJobRequirementUseCase createJobRequirementUseCase;
    private final GetJobRequirementUseCase getJobRequirementUseCase;
    private final DeleteJobRequirementUseCase deleteJobRequirementUseCase;

    public JobRequirementController(CreateJobRequirementUseCase createJobRequirementUseCase,
                                    GetJobRequirementUseCase getJobRequirementUseCase,
                                    DeleteJobRequirementUseCase deleteJobRequirementUseCase) {
        this.createJobRequirementUseCase = createJobRequirementUseCase;
        this.getJobRequirementUseCase = getJobRequirementUseCase;
        this.deleteJobRequirementUseCase = deleteJobRequirementUseCase;
    }

    @PostMapping("/job-requirements")
    public ResponseEntity<JobRequirementResponse> createJobRequirement(@Valid @RequestBody CreateJobRequirementRequest request) {
        JobRequirementResponse response = createJobRequirementUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/job-requirements/{id}")
    public ResponseEntity<JobRequirementResponse> getJobRequirementById(@PathVariable UUID id) {
        return ResponseEntity.ok(getJobRequirementUseCase.findById(id));
    }

    @GetMapping("/jobs/{jobId}/requirements")
    public ResponseEntity<List<JobRequirementResponse>> getJobRequirementsByJobId(@PathVariable UUID jobId) {
        return ResponseEntity.ok(getJobRequirementUseCase.findByJobId(jobId));
    }

    @DeleteMapping("/job-requirements/{id}")
    public ResponseEntity<Void> deleteJobRequirement(@PathVariable UUID id) {
        deleteJobRequirementUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
