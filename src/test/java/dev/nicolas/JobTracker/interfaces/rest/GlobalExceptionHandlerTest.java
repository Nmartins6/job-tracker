package dev.nicolas.JobTracker.interfaces.rest;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldReturnNotFoundForDomainNotFoundErrors() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDomainException(new DomainException("Usuário não encontrado pelo id 123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.NOT_FOUND.value());
    }

    @Test
    void shouldReturnBadRequestForOtherDomainErrors() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDomainException(new DomainException("Email já cadastrado"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.BAD_REQUEST.value());
    }
}
