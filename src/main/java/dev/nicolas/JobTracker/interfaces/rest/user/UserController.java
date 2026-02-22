package dev.nicolas.JobTracker.interfaces.rest.user;


import dev.nicolas.JobTracker.application.usecases.user.create.CreateUserUseCase;
import dev.nicolas.JobTracker.application.dto.user.CreateUserRequest;
import dev.nicolas.JobTracker.application.dto.user.UserResponse;
import dev.nicolas.JobTracker.application.usecases.user.get.GetUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final GetUserUseCase getUserUseCase;
    private final CreateUserUseCase createUserUseCase;

    public UserController(GetUserUseCase getUserUseCase, CreateUserUseCase createUserUseCase) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = createUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(getUserUseCase.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(getUserUseCase.findById(id));
    }
}