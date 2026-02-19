package dev.nicolas.JobTracker.application.usecases.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank(message = "Nome é obrigatório") String name,

    @NotBlank(message = "Email é obrigatório") @Email(message = "Email precisa ser válido") String email,

    @NotBlank(message = "Senha é obrigatória") @Size(min = 6, message = "Senha precisa ter pelo menos 6 caracteres") String password,

    String headline,
    String location,
    String bio){}
