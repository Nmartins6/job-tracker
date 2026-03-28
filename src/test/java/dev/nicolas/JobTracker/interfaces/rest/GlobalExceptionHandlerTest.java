package dev.nicolas.JobTracker.interfaces.rest;

import dev.nicolas.JobTracker.domain.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
    void shouldReturnNotFoundForFeminineDomainNotFoundErrors() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDomainException(new DomainException("Candidatura não encontrada pelo id 123"));

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

    @Test
    void shouldReturnBadRequestForMissingRequestParameter() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMissingRequestParameter(new MissingServletRequestParameterException("userId", "UUID"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).containsEntry("error", "Validation Error");
    }

    @Test
    void shouldReturnBadRequestForKnownIntegrityViolation() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleDataIntegrityViolation(new DataIntegrityViolationException(
                        "could not execute statement",
                        new RuntimeException("Unique index on users(email)")));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).containsEntry("error", "Domain Error");
        assertThat(response.getBody()).containsEntry("message", "Email já cadastrado");
    }

    @Test
    void shouldReturnMethodNotAllowedForUnsupportedMethod() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleMethodNotSupported(new HttpRequestMethodNotSupportedException("GET"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.METHOD_NOT_ALLOWED.value());
        assertThat(response.getBody()).containsEntry("error", "Method Not Allowed");
    }

    @Test
    void shouldReturnNotFoundForMissingResource() {
        ResponseEntity<Map<String, Object>> response =
                handler.handleResourceNotFound(new NoResourceFoundException(HttpMethod.GET, "/api/v1/users/123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody()).containsEntry("error", "Not Found");
    }
}
