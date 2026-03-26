package dev.nicolas.JobTracker.interfaces.rest.skill;

import dev.nicolas.JobTracker.application.dto.skill.CreateSkillRequest;
import dev.nicolas.JobTracker.application.dto.skill.SkillResponse;
import dev.nicolas.JobTracker.application.usecases.skill.create.CreateSkillUseCase;
import dev.nicolas.JobTracker.application.usecases.skill.get.GetSkillUseCase;
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
@RequestMapping("/api/v1/skills")
public class SkillController {

    private final CreateSkillUseCase createSkillUseCase;
    private final GetSkillUseCase getSkillUseCase;

    public SkillController(CreateSkillUseCase createSkillUseCase, GetSkillUseCase getSkillUseCase) {
        this.createSkillUseCase = createSkillUseCase;
        this.getSkillUseCase = getSkillUseCase;
    }

    @PostMapping
    public ResponseEntity<SkillResponse> createSkill(@Valid @RequestBody CreateSkillRequest request) {
        SkillResponse response = createSkillUseCase.execute(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        return ResponseEntity.ok(getSkillUseCase.findAll());
    }
}
