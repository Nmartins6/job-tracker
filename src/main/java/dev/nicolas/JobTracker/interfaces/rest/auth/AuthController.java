package dev.nicolas.JobTracker.interfaces.rest.auth;

import dev.nicolas.JobTracker.application.dto.auth.AuthenticatedUserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @GetMapping("/me")
    public ResponseEntity<AuthenticatedUserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(new AuthenticatedUserResponse(authentication.getName()));
    }
}
