package dev.nicolas.JobTracker.application.usecases.user.create;

import dev.nicolas.JobTracker.application.dto.user.CreateUserRequest;
import dev.nicolas.JobTracker.application.dto.user.UserResponse;
import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;

    public CreateUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        String normalizedEmail = normalizeEmail(request.email());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DomainException("Email já cadastrado: " + normalizedEmail);
        }

        User user = User.create(
                request.name(),
                normalizedEmail,
                request.password(), //TODO: hash with BCrypt?
                request.headline(),
                request.location(),
                request.bio());

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getHeadLine(),
                saved.getLocation(),
                saved.getBio());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

}
