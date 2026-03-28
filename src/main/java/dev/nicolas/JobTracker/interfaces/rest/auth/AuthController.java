package dev.nicolas.JobTracker.interfaces.rest.auth;

import dev.nicolas.JobTracker.application.dto.auth.AuthenticatedUserResponse;
import dev.nicolas.JobTracker.domain.user.User;
import dev.nicolas.JobTracker.domain.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(Authentication authentication) {
        User user = userRepository.findByEmail(normalizeEmail(authentication.getName()))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário autenticado não encontrado pelo email " + authentication.getName()));

        return ResponseEntity.ok(new AuthenticatedUserResponse(
                user.getId(),
                user.getName(),
                user.getEmail()));
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
