package dev.nicolas.JobTracker.interfaces.rest.userSkill;

import dev.nicolas.JobTracker.application.dto.userSkill.CreateUserSkillRequest;
import dev.nicolas.JobTracker.application.dto.userSkill.UserSkillResponse;
import dev.nicolas.JobTracker.application.usecases.userSkill.create.CreateUserSkillUseCase;
import dev.nicolas.JobTracker.application.usecases.userSkill.get.GetUserSkillUseCase;
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
public class UserSkillController {

    private final CreateUserSkillUseCase createUserSkillUseCase;
    private final GetUserSkillUseCase getUserSkillUseCase;

    public UserSkillController(CreateUserSkillUseCase createUserSkillUseCase,
                               GetUserSkillUseCase getUserSkillUseCase) {
        this.createUserSkillUseCase = createUserSkillUseCase;
        this.getUserSkillUseCase = getUserSkillUseCase;
    }

    @PostMapping("/user-skills")
    public ResponseEntity<UserSkillResponse> createUserSkill(@Valid @RequestBody CreateUserSkillRequest request) {
        UserSkillResponse response = createUserSkillUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/user-skills/{id}")
    public ResponseEntity<UserSkillResponse> getUserSkillById(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserSkillUseCase.findById(id));
    }

    @GetMapping("/users/{userId}/skills")
    public ResponseEntity<List<UserSkillResponse>> getUserSkillsByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(getUserSkillUseCase.findByUserId(userId));
    }
}
