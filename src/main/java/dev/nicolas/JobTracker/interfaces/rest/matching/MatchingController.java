package dev.nicolas.JobTracker.interfaces.rest.matching;

import dev.nicolas.JobTracker.application.dto.matching.JobMatchingResponse;
import dev.nicolas.JobTracker.application.usecases.matching.get.GetJobMatchingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class MatchingController {

    private final GetJobMatchingUseCase getJobMatchingUseCase;

    public MatchingController(GetJobMatchingUseCase getJobMatchingUseCase) {
        this.getJobMatchingUseCase = getJobMatchingUseCase;
    }

    @GetMapping("/jobs/{jobId}/matching")
    public ResponseEntity<JobMatchingResponse> getJobMatching(@PathVariable UUID jobId,
                                                              @RequestParam UUID userId) {
        return ResponseEntity.ok(getJobMatchingUseCase.execute(userId, jobId));
    }
}
