package dev.nicolas.JobTracker.interfaces.rest.job;

import dev.nicolas.JobTracker.application.dto.job.CreateJobRequest;
import dev.nicolas.JobTracker.application.dto.job.JobResponse;
import dev.nicolas.JobTracker.application.usecases.job.create.CreateJobUseCase;
import dev.nicolas.JobTracker.application.usecases.job.get.GetJobUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final CreateJobUseCase createJobUseCase;
    private final GetJobUseCase getJobUseCase;

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
