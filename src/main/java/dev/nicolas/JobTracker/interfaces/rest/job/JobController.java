package dev.nicolas.JobTracker.interfaces.rest.job;

import dev.nicolas.JobTracker.application.dto.job.CreateJobRequest;
import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.application.usecases.job.create.CreateJobUseCase;
import dev.nicolas.JobTracker.application.usecases.job.get.GetJobUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    public CreateJobUseCase createJobUseCase;
    public GetJobUseCase getJobUseCase;

    public JobController(CreateJobUseCase createJobUseCase, GetJobUseCase getJobUseCase) {
        this.createJobUseCase = createJobUseCase;
        this.getJobUseCase = getJobUseCase;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobResponse response = createJobUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        return ResponseEntity.ok(getJobUseCase.findAll());
    }
}
